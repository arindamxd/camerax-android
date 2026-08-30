package com.arindam.camerax.data.camera

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import com.arindam.camerax.util.commons.Constants
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Data: horizontal sweep stitcher for CameraX stills. Aligns neighboring frames by
 * minimizing overlap error, then cross-fades the seam. Call via
 * [com.arindam.camerax.domain.usecase.StitchPanorama] / [com.arindam.camerax.domain.repository.MediaRepository]
 * — keep off the main thread.
 */
object PanoramaStitcher {

    fun stitch(frames: List<File>, outputDirectory: File): File {
        require(frames.isNotEmpty()) { "Panorama needs at least one frame" }
        val bitmaps = frames.mapNotNull { decode(it) }
        require(bitmaps.isNotEmpty()) { "Unable to decode panorama frames" }
        val result = if (bitmaps.size == 1) bitmaps[0] else merge(bitmaps)
        val output = File(
            outputDirectory,
            SimpleDateFormat(Constants.FILE.FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis()) + Constants.FILE.PHOTO_EXTENSION
        )
        StillImageExif.writeJpeg(result, output, quality = 92)
        if (result !== bitmaps[0]) result.recycle()
        bitmaps.forEach { it.recycle() }
        return output
    }

    private fun decode(file: File): Bitmap? = StillImageExif.decodeSampled(file, MAX_EDGE)

    private fun merge(frames: List<Bitmap>): Bitmap {
        val height = frames.minOf { it.height }
        val scaled = frames.map { scaleToHeight(it, height) }
        val offsets = IntArray(scaled.size)
        val yOffsets = IntArray(scaled.size)
        for (index in 1 until scaled.size) {
            val (overlap, dy) = estimateOverlap(scaled[index - 1], scaled[index])
            offsets[index] = offsets[index - 1] + scaled[index - 1].width - overlap
            yOffsets[index] = yOffsets[index - 1] + dy
        }
        
        // Center the vertical crop
        val minY = yOffsets.minOrNull() ?: 0
        val maxY = yOffsets.maxOrNull() ?: 0
        val outHeight = height + (maxY - minY)
        val width = offsets.last() + scaled.last().width
        
        val output = Bitmap.createBitmap(width, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        scaled.forEachIndexed { index, bitmap ->
            val yOffset = (yOffsets[index] - minY).toFloat()
            if (index == 0) {
                canvas.drawBitmap(bitmap, 0f, yOffset, paint)
            } else {
                val overlap = scaled[index - 1].width - (offsets[index] - offsets[index - 1])
                drawBlended(canvas, bitmap, offsets[index], yOffset, overlap.coerceAtLeast(1), outHeight)
            }
        }
        scaled.filter { it !in frames }.forEach { it.recycle() }
        return output
    }

    private fun scaleToHeight(source: Bitmap, height: Int): Bitmap {
        if (source.height == height) return source
        val width =
            (source.width * (height / source.height.toFloat())).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, width, height, true)
    }

    private fun estimateOverlap(left: Bitmap, right: Bitmap): Pair<Int, Int> {
        val minOverlap = (left.width * 0.10f).roundToInt().coerceAtLeast(24)
        val maxOverlap = (min(left.width, right.width) * 0.90f).roundToInt()
        val step = max(4, (maxOverlap - minOverlap) / 48)
        val sampleY = max(1, left.height / 80)
        
        var bestOverlap = (left.width * 0.3f).roundToInt()
        var bestDy = 0
        var bestError = Long.MAX_VALUE

        // Pre-allocate row buffers for bulk pixel reads (avoids per-pixel JNI overhead).
        val maxWidth = max(left.width, right.width)
        val leftRow = IntArray(maxWidth)
        val rightRow = IntArray(maxWidth)

        val maxYShift = (left.height * 0.05f).roundToInt()
        val dyStep = max(1, maxYShift / 5)

        var overlap = minOverlap
        while (overlap <= maxOverlap) {
            var dy = -maxYShift
            while (dy <= maxYShift) {
                var error = 0L
                var samples = 0
                var y = max(0, dy)
                val maxY = min(left.height, left.height + dy)
                
                while (y < maxY) {
                    // Bulk-read the overlap region from both bitmaps.
                    left.getPixels(leftRow, 0, overlap, left.width - overlap, y, overlap, 1)
                    right.getPixels(rightRow, 0, overlap, 0, y - dy, overlap, 1)
                    var x = 0
                    while (x < overlap) {
                        val leftPixel = leftRow[x]
                        val rightPixel = rightRow[x]
                        error += abs(((leftPixel shr 16) and 0xFF) - ((rightPixel shr 16) and 0xFF))
                        error += abs(((leftPixel shr 8) and 0xFF) - ((rightPixel shr 8) and 0xFF))
                        error += abs((leftPixel and 0xFF) - (rightPixel and 0xFF))
                        samples++
                        x += 8
                    }
                    y += sampleY
                }
                val mean = if (samples == 0) Long.MAX_VALUE else error / samples
                if (mean < bestError) {
                    bestError = mean
                    bestOverlap = overlap
                    bestDy = dy
                }
                dy += dyStep
            }
            overlap += step
        }
        return Pair(bestOverlap, bestDy)
    }

    private fun drawBlended(
        canvas: Canvas,
        bitmap: Bitmap,
        left: Int,
        top: Float,
        overlap: Int,
        outHeight: Int
    ) {
        val save = canvas.saveLayer(
            left.toFloat(),
            0f,
            (left + bitmap.width).toFloat(),
            outHeight.toFloat(),
            null
        )
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(bitmap, left.toFloat(), top, paint)
        if (overlap > 1) {
            paint.shader = LinearGradient(
                left.toFloat(),
                0f,
                (left + overlap).toFloat(),
                0f,
                0x00FFFFFF,
                0xFFFFFFFF.toInt(),
                Shader.TileMode.CLAMP
            )
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            canvas.drawRect(
                left.toFloat(),
                0f,
                (left + overlap).toFloat(),
                outHeight.toFloat(),
                paint
            )
        }
        canvas.restoreToCount(save)
    }

    private const val MAX_EDGE = 1600
}
