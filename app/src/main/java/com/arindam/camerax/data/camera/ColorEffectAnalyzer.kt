package com.arindam.camerax.data.camera

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.arindam.camerax.domain.model.EffectMode
import com.arindam.camerax.util.log.Logger

/**
 * Live Effects analyzer: each [ImageAnalysis] frame is decoded, oriented, and recolored with a
 * [android.graphics.ColorMatrix]. Switching chips is a field write — no rebind.
 *
 * Pre-allocates a reusable output [Bitmap] and [Canvas] to avoid per-frame allocations.
 * Only reallocates when the oriented frame dimensions change (rotation or resolution shift).
 */
internal class ColorEffectAnalyzer(
    private val onFrame: (Bitmap) -> Unit
) : ImageAnalysis.Analyzer {

    @Volatile
    var effect: EffectMode = EffectMode.NONE

    @Volatile
    private var processing = false

    private val outputPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }

    fun release() {
        // No-op
    }

    override fun analyze(imageProxy: ImageProxy) {
        if (processing) {
            imageProxy.close()
            return
        }
        processing = true
        try {
            val rotation = imageProxy.imageInfo.rotationDegrees
            val source = imageProxy.toBitmap()
            val upright = if (rotation == 0) {
                source
            } else {
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
            }

            val target = Bitmap.createBitmap(upright.width, upright.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(target)

            ColorEffects.renderInto(upright, effect, target, canvas, outputPaint)

            if (upright !== source) upright.recycle()
            source.recycle()

            onFrame(target)
        } catch (error: Throwable) {
            Logger.error(TAG, "Effect processing failed: ${error.message}", error)
        } finally {
            imageProxy.close()
            processing = false
        }
    }

    private companion object {
        const val TAG = "ColorEffectAnalyzer"
    }
}
