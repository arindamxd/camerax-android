package com.arindam.camerax.ui.home.camera

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.StringRes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import com.arindam.camerax.R
import com.arindam.camerax.domain.model.CameraExtension
import com.arindam.camerax.domain.model.CameraLens
import com.arindam.camerax.domain.model.CameraMode
import com.arindam.camerax.domain.model.CameraModeCatalog
import com.arindam.camerax.domain.model.CaptureAction
import com.arindam.camerax.domain.model.CaptureAspect
import com.arindam.camerax.domain.model.DeviceCaptureFeatures
import com.arindam.camerax.domain.model.EffectMode
import com.arindam.camerax.domain.model.ExposureLimits
import com.arindam.camerax.domain.model.ExposurePriority
import com.arindam.camerax.domain.model.LastCameraSession
import com.arindam.camerax.domain.model.FlashMode
import com.arindam.camerax.domain.model.NightScene
import com.arindam.camerax.domain.model.PhysicalZoom
import com.arindam.camerax.domain.model.SlowMotionRate
import com.arindam.camerax.domain.model.StillFormat
import com.arindam.camerax.domain.model.TimerMode
import com.arindam.camerax.domain.model.VideoHdrRange
import com.arindam.camerax.domain.model.VideoQuality
import java.io.File

/** String resource for the mode pager chip. Add a mapping when you add a [CameraMode]. */
val CameraMode.labelRes: Int
    @StringRes get() = when (this) {
        CameraMode.PHOTO -> R.string.mode_photo
        CameraMode.VIDEO -> R.string.mode_video
        CameraMode.SLOW_MOTION -> R.string.mode_slow_motion
        CameraMode.EFFECTS -> R.string.mode_effects
        CameraMode.PANORAMA -> R.string.mode_panorama
        CameraMode.DUAL -> R.string.mode_dual
        CameraMode.OTHERS -> R.string.mode_others
    }

val FlashMode.labelRes: Int
    @StringRes get() = when (this) {
        FlashMode.OFF -> R.string.flash_off
        FlashMode.ON -> R.string.flash_on
        FlashMode.AUTO -> R.string.flash_auto
        FlashMode.TORCH -> R.string.flash_torch
    }

val TimerMode.labelRes: Int
    @StringRes get() = when (this) {
        TimerMode.OFF -> R.string.timer_off
        TimerMode.THREE -> R.string.timer_3
        TimerMode.TEN -> R.string.timer_10
    }

val CameraExtension.labelRes: Int
    @StringRes get() = when (this) {
        CameraExtension.NONE -> R.string.extension_none
        CameraExtension.HDR -> R.string.extension_hdr
        CameraExtension.NIGHT -> R.string.extension_night
        CameraExtension.PORTRAIT -> R.string.extension_portrait
        CameraExtension.BEAUTY -> R.string.extension_beauty
    }

val ExposurePriority.labelRes: Int
    @StringRes get() = when (this) {
        ExposurePriority.AUTO -> R.string.ae_auto
        ExposurePriority.ISO -> R.string.ae_iso
        ExposurePriority.SHUTTER -> R.string.ae_shutter
    }

val EffectMode.labelRes: Int
    @StringRes get() = when (this) {
        EffectMode.NONE -> R.string.effect_none
        EffectMode.GRAYSCALE -> R.string.effect_grayscale
        EffectMode.INVERT -> R.string.effect_invert
        EffectMode.SEPIA -> R.string.effect_sepia
        EffectMode.COOL -> R.string.effect_cool
        EffectMode.WARM -> R.string.effect_warm
        EffectMode.VIVID -> R.string.effect_vivid
    }

val EffectMode.styleTitleRes: Int
    @StringRes get() = when (this) {
        EffectMode.NONE -> R.string.style_standard
        EffectMode.VIVID -> R.string.style_vibrant
        EffectMode.WARM -> R.string.style_warm
        EffectMode.COOL -> R.string.style_cool
        EffectMode.SEPIA -> R.string.style_dramatic
        EffectMode.GRAYSCALE -> R.string.style_mono
        EffectMode.INVERT -> R.string.style_invert
    }

val CaptureAspect.labelRes: Int
    @StringRes get() = when (this) {
        CaptureAspect.RATIO_4_3 -> R.string.pref_aspect_4_3
        CaptureAspect.RATIO_16_9 -> R.string.pref_aspect_16_9
        CaptureAspect.FULL -> R.string.pref_aspect_full
    }

val CaptureAspect.shortLabel: String
    get() = when (this) {
        CaptureAspect.RATIO_4_3 -> "4:3"
        CaptureAspect.RATIO_16_9 -> "16:9"
        CaptureAspect.FULL -> "FULL"
    }

/**
 * Presentation state for the live feed. Chrome flags are derived from
 * [com.arindam.camerax.domain.model.CameraModeCatalog]; do not scatter `if (mode == …)` in UI.
 */
data class CameraUiState(
    val mode: CameraMode = CameraMode.PHOTO,
    val lens: CameraLens = CameraLens.BACK,
    val flash: FlashMode = FlashMode.OFF,
    val timer: TimerMode = TimerMode.OFF,
    val gridEnabled: Boolean = false,
    val countdownRemaining: Int? = null,
    val zoomRatio: Float = 1f,
    val minZoom: Float = 1f,
    val maxZoom: Float = 1f,
    val hasFlash: Boolean = false,
    val isCameraReady: Boolean = true,
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val isMuted: Boolean = false,
    val microphonePermissionGranted: Boolean = true,
    val recordingNanos: Long = 0L,
    val recordingSizeBytes: Long = 0L,
    val thumbnail: File? = null,
    val extension: CameraExtension = CameraExtension.NONE,
    val supportedExtensions: Set<CameraExtension> = emptySet(),
    val effect: EffectMode = EffectMode.NONE,
    val effectFrame: ImageBitmap? = null,
    val focusPoint: Offset? = null,
    val captureFlashToken: Int = 0,
    val bindRevision: Int = 0,
    val lockCaptureMode: Boolean = false,
    val nightScene: NightScene = NightScene.UNKNOWN,
    val autoNightActive: Boolean = false,
    val motionPhotoEnabled: Boolean = false,
    val motionCapturing: Boolean = false,
    val ultraHdrEnabled: Boolean = false,
    val stillFormat: StillFormat = StillFormat.JPEG,
    val captureAspect: CaptureAspect = CaptureAspect.RATIO_4_3,
    val videoQuality: VideoQuality = VideoQuality.FHD,
    val videoHdrRange: VideoHdrRange = VideoHdrRange.SDR,
    val videoHdrBound: VideoHdrRange = VideoHdrRange.SDR,
    val videoStabilization: Boolean = true,
    val videoStabilizationActive: Boolean = false,
    val slowMotionQuality: VideoQuality = VideoQuality.FHD,
    val slowMotionRate: SlowMotionRate = SlowMotionRate.AUTO,
    val ultraHdr: Boolean = false,
    val rawCapture: Boolean = false,
    val rawFullSensor: Boolean = false,
    val exposurePriority: ExposurePriority = ExposurePriority.AUTO,
    val exposureLimits: ExposureLimits = ExposureLimits(),
    val iso: Int = 100,
    val shutterNanos: Long = 16_666_667L,
    val exposureCompensation: Int = 0,
    val panoramaActive: Boolean = false,
    val panoramaFrames: List<File> = emptyList(),
    val cameraId: String? = null,
    val physicalZooms: List<PhysicalZoom> = emptyList(),
    val slowMotionSupported: Boolean = false,
    val slowMotionFps: Int = 0,
    val lowLightBoost: Boolean = true,
    val lowLightBoostSupported: Boolean = false,
    val lowLightBoostActive: Boolean = false,
    val flipWhileRecording: Boolean = false,
    val frontMirror: Boolean = true,
    val review: CaptureReview? = null,
    val concurrentSupported: Boolean = false,
    val videoFps60: Boolean = false,
    val videoFps60Supported: Boolean = false,
    val videoFps60Active: Boolean = false,
    val deviceFeatures: DeviceCaptureFeatures = DeviceCaptureFeatures(),
    val lastSession: LastCameraSession? = null,
    val message: String? = null,
    val messageRes: Int? = null
) {
    val zoomChips: List<Float>
        get() {
            val chips = mutableListOf<Float>()
            // 1. Extra physical cameras (e.g. 0.5x ultrawide, telephoto)
            physicalZooms.forEach { chips.add(it.label) }
            // 2. Ultrawide: only include if hardware supports < 0.95x and no physical ultrawide already added
            if (minZoom < 0.95f && chips.none { it < 0.95f }) {
                val roundedMin = (kotlin.math.round(minZoom * 10f) / 10f).coerceAtLeast(0.1f)
                chips.add(roundedMin)
            }
            // 3. Base 1.0x optical standard is always available
            chips.add(1.0f)
            // 4. 2x telephoto/digital step if supported by the hardware maxZoom
            if (maxZoom >= 1.95f) {
                chips.add(2.0f)
            }
            return chips.distinct().sorted()
        }

    val activeZoomChip: Float
        get() {
            val currentPhysical = physicalZooms.firstOrNull { it.cameraId == cameraId }
            if (currentPhysical != null && currentPhysical.label < 1.0f) {
                return currentPhysical.label
            }
            return zoomChips.minByOrNull { kotlin.math.abs(it - zoomRatio) } ?: zoomRatio
        }

    val visibleModes: List<CameraMode>
        get() = CameraModeCatalog.visibleModes(slowMotionSupported, concurrentSupported)

    private val profile get() = CameraModeCatalog.profile(mode)

    val isVideoMode: Boolean
        get() = profile.captureAction == CaptureAction.VIDEO || mode == CameraMode.VIDEO

    val showsFlash: Boolean
        get() = hasFlash && profile.showsFlash

    val showsTimer: Boolean
        get() = profile.showsTimer

    val showsGrid: Boolean
        get() = profile.showsGrid

    val showsMotion: Boolean
        get() = profile.showsMotion && profile.allowsMotionPhoto && !rawCapture

    val showsZoomChips: Boolean
        get() = profile.showsZoom

    val showsStillBadge: Boolean
        get() = profile.showsStillBadge &&
            (stillFormat == StillFormat.RAW_JPEG || ultraHdrEnabled)

    val showsVideoStatus: Boolean
        get() = profile.showsVideoStatus

    val showsExposureControls: Boolean
        get() = profile.showsExposure &&
            (exposureLimits.supportedPriorities.size >= 2 ||
                exposureLimits.evSupported)

    val showsEffects: Boolean
        get() = profile.showsEffects

    val allowsEffect: Boolean
        get() = profile.allowsEffect

    val showsAspectControl: Boolean
        get() = (mode == CameraMode.PHOTO || mode == CameraMode.EFFECTS) &&
            !isRecording && !panoramaActive

    val showsPip: Boolean
        get() = profile.showsPip

    val showsTools: Boolean
        get() = profile.showsTools

    val showsCaptureControls: Boolean
        get() = profile.showsCaptureControls

    val showsNightHint: Boolean
        get() = profile.showsNightHint

    val showsLowLightBoost: Boolean
        get() = profile.showsLowLightBoost

    val showsAudioMuteControl: Boolean
        get() = profile.allowsAudioMute

    val allowsAudioMute: Boolean
        get() = profile.allowsAudioMute && microphonePermissionGranted

    val captureAction: CaptureAction
        get() = profile.captureAction

    val showsFlipControl: Boolean
        get() = profile.showsFlip &&
            (!isRecording || (flipWhileRecording && profile.allowsPersistentRecording))

    val recordsVideo: Boolean
        get() = profile.captureAction == CaptureAction.VIDEO

    val showsSlowMotionFps: Boolean
        get() = profile.bindSlowMotion
}

/**
 * Presentation: optional Retake/Done review payload after a still or clip.
 * Built by [CameraViewModel]; rendered by [CaptureConfirmOverlay].
 */
data class CaptureReview(
    val file: File,
    val isVideo: Boolean,
    val isMotionPhoto: Boolean,
    val width: Int,
    val height: Int,
    val durationLabel: String? = null,
    @StringRes val formatLabelRes: Int? = null,
    val companions: List<File> = emptyList()
) {
    fun metadataLabel(formatText: String?): String {
        val size = if (width > 0 && height > 0) "$width × $height" else null
        return listOfNotNull(durationLabel, size, formatText).joinToString(" · ")
    }
}

/** Presentation: system intent that launched the camera (IMAGE_CAPTURE, VIDEO_CAPTURE, …). */
enum class ExternalCaptureKind {
    NONE,
    IMAGE_CAPTURE,
    VIDEO_CAPTURE,
    MOTION_PHOTO,
    OPEN_PHOTO,
    OPEN_VIDEO
}

/**
 * Presentation: parsed capture intent + optional `EXTRA_OUTPUT` Uri.
 * [CameraFragment] / [CameraViewModel] deliver results for [returnsResult] kinds.
 */
data class ExternalCaptureRequest(
    val kind: ExternalCaptureKind = ExternalCaptureKind.NONE,
    val outputUri: Uri? = null
) {
    val returnsResult: Boolean
        get() = kind == ExternalCaptureKind.IMAGE_CAPTURE ||
            kind == ExternalCaptureKind.VIDEO_CAPTURE ||
            kind == ExternalCaptureKind.MOTION_PHOTO

    companion object {
        fun from(intent: Intent): ExternalCaptureRequest {
            val output = extraOutputUri(intent)
            return when (intent.action) {
                MediaStore.ACTION_IMAGE_CAPTURE,
                MediaStore.ACTION_IMAGE_CAPTURE_SECURE ->
                    ExternalCaptureRequest(ExternalCaptureKind.IMAGE_CAPTURE, output)
                MediaStore.ACTION_VIDEO_CAPTURE ->
                    ExternalCaptureRequest(ExternalCaptureKind.VIDEO_CAPTURE, output)
                MOTION_PHOTO_CAPTURE,
                MOTION_PHOTO_CAPTURE_SECURE ->
                    ExternalCaptureRequest(ExternalCaptureKind.MOTION_PHOTO, output)
                MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA,
                MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE ->
                    ExternalCaptureRequest(ExternalCaptureKind.OPEN_PHOTO)
                MediaStore.INTENT_ACTION_VIDEO_CAMERA ->
                    ExternalCaptureRequest(ExternalCaptureKind.OPEN_VIDEO)
                else -> ExternalCaptureRequest()
            }
        }

        private fun extraOutputUri(intent: Intent): Uri? {
            val extra = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT)
            }
            return extra ?: intent.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.uri
        }

        private const val MOTION_PHOTO_CAPTURE = "android.provider.action.MOTION_PHOTO_CAPTURE"
        private const val MOTION_PHOTO_CAPTURE_SECURE =
            "android.provider.action.MOTION_PHOTO_CAPTURE_SECURE"
    }
}

fun formatRecordingTime(nanos: Long): String {
    val totalSeconds = nanos / 1_000_000_000L
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

fun formatFileSize(bytes: Long): String = when {
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024L -> "%.1f KB".format(bytes / 1024.0)
    bytes < 1024L * 1024L * 1024L -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    else -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
}
