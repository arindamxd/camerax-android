package com.arindam.camerax.ui.home.camera

import android.media.MediaMetadataRetriever
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MotionPhotosOff
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Timer3
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.roundToInt
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.arindam.camerax.R
import com.arindam.camerax.domain.model.CameraExtension
import com.arindam.camerax.domain.model.CameraMode
import com.arindam.camerax.domain.model.CaptureAction
import com.arindam.camerax.domain.model.CaptureAspect
import com.arindam.camerax.domain.model.EffectMode
import com.arindam.camerax.domain.model.ExposurePriority
import com.arindam.camerax.domain.model.FlashMode
import com.arindam.camerax.domain.model.NightScene
import com.arindam.camerax.domain.model.StillFormat
import com.arindam.camerax.domain.model.TimerMode
import com.arindam.camerax.domain.model.VideoHdrRange
import com.arindam.camerax.domain.model.VideoQuality
import com.arindam.camerax.ui.compose.CameraGlassButton
import com.arindam.camerax.ui.theme.CameraAccent
import com.arindam.camerax.ui.theme.CameraDanger
import com.arindam.camerax.ui.theme.CameraFontFamily
import com.arindam.camerax.ui.theme.CameraGlass
import com.arindam.camerax.ui.theme.CameraGlassStrong
import com.arindam.camerax.ui.theme.CameraMono
import com.arindam.camerax.ui.theme.CameraOnGlass
import com.arindam.camerax.ui.theme.CameraOnGlassMuted
import com.arindam.camerax.ui.theme.themedOverlayChrome
import java.io.File

/**
 * iOS-styled crescent moon icon.
 */
@Composable
fun NightModeMoonIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val outer = Path().apply {
            addOval(Rect(0f, 0f, w, h))
        }
        val inner = Path().apply {
            addOval(Rect(w * 0.35f, -h * 0.08f, w * 1.25f, h * 0.92f))
        }
        val moonPath = Path().apply {
            op(outer, inner, PathOperation.Difference)
        }
        drawPath(moonPath, color = tint)
    }
}

/**
 * iOS-styled Night Mode indicator badge.
 */
@Composable
fun NightModeBadge(
    isActive: Boolean,
    compact: Boolean = false,
    onClick: () -> Unit
) {
    val height = if (compact) 32.dp else 36.dp
    val bgColor = if (isActive) CameraAccent else CameraGlassStrong
    val contentColor = if (isActive) Color.Black else CameraOnGlass
    val borderColor = if (isActive) Color.Transparent else Color.White.copy(alpha = 0.15f)

    Row(
        modifier = Modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(height / 2))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        NightModeMoonIcon(
            modifier = Modifier.size(if (compact) 14.dp else 16.dp),
            tint = contentColor
        )
        Text(
            text = if (isActive) "Auto" else "Off",
            color = contentColor,
            fontFamily = CameraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 11.sp else 12.sp
        )
    }
}

/**
 * iOS-styled video resolution and frame rate capsule (e.g. [ 4K · 60 ] or [ HD · 30 ]).
 */
@Composable
fun VideoFormatPill(
    state: CameraUiState,
    compact: Boolean = false,
    onCycleQuality: () -> Unit = {},
    onToggleFps: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val height = if (compact) 30.dp else 34.dp
    val qualityLabel = when (state.videoQuality) {
        VideoQuality.UHD -> "4K"
        VideoQuality.FHD -> "HD"
        VideoQuality.HD -> "720p"
        VideoQuality.SD -> "480p"
    }
    val fpsLabel = when {
        state.mode == CameraMode.SLOW_MOTION -> "${state.slowMotionFps.takeIf { it > 0 } ?: 120}"
        state.videoFps60Active || state.videoFps60 -> "60"
        else -> "30"
    }

    Row(
        modifier = Modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(CameraGlassStrong)
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(height / 2))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = qualityLabel,
            color = Color.White,
            fontFamily = CameraMono,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 11.sp else 12.sp,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = 16.dp),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCycleQuality()
                    }
                )
                .padding(horizontal = 3.dp, vertical = 2.dp)
        )
        Text(
            text = "·",
            color = Color.White.copy(alpha = 0.4f),
            fontFamily = CameraMono,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 11.sp else 12.sp
        )
        Text(
            text = fpsLabel,
            color = if (state.videoFps60Active || state.videoFps60) CameraAccent else Color.White,
            fontFamily = CameraMono,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 11.sp else 12.sp,
            modifier = Modifier
                .clickable(
                    enabled = state.mode != CameraMode.SLOW_MOTION && state.videoFps60Supported,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = 16.dp),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleFps()
                    }
                )
                .padding(horizontal = 3.dp, vertical = 2.dp)
        )
    }
}

/**
 * iOS 17/18-style virtual horizon level indicator.
 */
@Composable
fun HorizonLevelIndicator(
    rollAngle: Float,
    isFlat: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isLevel = kotlin.math.abs(rollAngle) < 0.75f
    val inRange = kotlin.math.abs(rollAngle) in 0.0f..14.0f && !isFlat

    var wasLevel by remember { mutableStateOf(false) }
    var levelDurationMs by remember { mutableLongStateOf(0L) }
    var visibleAlpha by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isLevel) {
        if (isLevel && !wasLevel) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            wasLevel = true
            levelDurationMs = System.currentTimeMillis()
        } else if (!isLevel) {
            wasLevel = false
        }
    }

    LaunchedEffect(inRange, isLevel, wasLevel, levelDurationMs) {
        if (!inRange) {
            visibleAlpha = 0f
            return@LaunchedEffect
        }
        if (isLevel) {
            visibleAlpha = 1f
            delay(1500)
            visibleAlpha = 0f
        } else {
            visibleAlpha = 1f
        }
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = visibleAlpha,
        animationSpec = tween(durationMillis = 220),
        label = "levelAlpha"
    )

    if (animatedAlpha > 0.01f) {
        Box(
            modifier = modifier
                .size(width = 120.dp, height = 30.dp)
                .alpha(animatedAlpha)
                .graphicsLayer {
                    rotationZ = -rollAngle
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val barColor = if (isLevel) CameraAccent else Color.White.copy(alpha = 0.85f)
                val strokeW = if (isLevel) 2.2.dp.toPx() else 1.6.dp.toPx()

                if (isLevel) {
                    // Continuous solid yellow line when leveled
                    drawLine(
                        color = barColor,
                        start = Offset(cx - 36.dp.toPx(), cy),
                        end = Offset(cx + 36.dp.toPx(), cy),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                } else {
                    // Broken line: left bar, center gap with small dot, right bar
                    val gapHalf = 12.dp.toPx()
                    val barLen = 22.dp.toPx()

                    // Left segment
                    drawLine(
                        color = barColor,
                        start = Offset(cx - gapHalf - barLen, cy),
                        end = Offset(cx - gapHalf, cy),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                    // Center reference mark
                    drawCircle(
                        color = barColor,
                        radius = 1.4.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                    // Right segment
                    drawLine(
                        color = barColor,
                        start = Offset(cx + gapHalf, cy),
                        end = Offset(cx + gapHalf + barLen, cy),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

/**
 * Overlay chrome (header, HUD, zoom, footer). Visibility flags come from
 * [com.arindam.camerax.domain.model.CameraModeProfile] via [CameraUiState].
 */
@Composable
fun CameraHeader(
    state: CameraUiState,
    compact: Boolean = false,
    drawerOpen: Boolean = false,
    onToggleDrawer: () -> Unit = {},
    stylesOpen: Boolean = false,
    onStylesToggle: () -> Unit = {},
    onNightClicked: () -> Unit = {},
    onCycleVideoQuality: () -> Unit = {},
    onToggleVideoFps: () -> Unit = {},
    onFlashClicked: () -> Unit,
    onTimerClicked: () -> Unit,
    onGridClicked: () -> Unit,
    onMotionClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onExposurePrioritySelected: (ExposurePriority) -> Unit = {},
    onIsoChanged: (Int) -> Unit = {},
    onShutterChanged: (Long) -> Unit = {},
    onCompensationChanged: (Int) -> Unit = {}
) {
    var exposureOpen by remember { mutableStateOf(false) }
    val chrome = if (state.showsTools) themedOverlayChrome() else null
    LaunchedEffect(state.showsExposureControls) {
        if (!state.showsExposureControls) exposureOpen = false
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    if (chrome != null) {
                        listOf(chrome.canvas, Color.Transparent)
                    } else {
                        listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent)
                    }
                )
            )
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            val controlSize = if (compact) 40.dp else 44.dp
            // Left: Flash toggle + Night Mode Badge
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (state.showsFlash) {
                    GlassIconButton(
                        icon = flashIcon(state.flash),
                        contentDescription = stringResource(state.flash.labelRes),
                        selected = state.flash != FlashMode.OFF,
                        compact = compact,
                        tooltip = true,
                        onClick = onFlashClicked
                    )
                }
                val showNight = state.showsNightHint &&
                    (state.nightScene == NightScene.RECOMMENDED ||
                        state.autoNightActive ||
                        state.extension == CameraExtension.NIGHT) &&
                    !state.isRecording
                AnimatedVisibility(
                    visible = showNight,
                    enter = fadeIn(tween(250)) + expandHorizontally(),
                    exit = fadeOut(tween(200)) + shrinkHorizontally()
                ) {
                    val isNightActive = state.extension == CameraExtension.NIGHT || state.autoNightActive
                    NightModeBadge(
                        isActive = isNightActive,
                        compact = compact,
                        onClick = onNightClicked
                    )
                }
            }

            // Center: Chevron drawer toggle indicator
            Box(
                modifier = Modifier
                    .size(controlSize)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                if (!state.isRecording && !state.panoramaActive) {
                    Box(
                        modifier = Modifier
                            .size(if (compact) 32.dp else 36.dp)
                            .clip(CircleShape)
                            .background(if (drawerOpen) CameraAccent else CameraGlassStrong)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = onToggleDrawer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (drawerOpen) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                            contentDescription = stringResource(
                                if (drawerOpen) R.string.collapse_controls else R.string.expand_controls
                            ),
                            tint = if (drawerOpen) Color.Black else CameraOnGlass,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Right: Motion Photo + Styles + VideoFormatPill + Settings
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (state.isVideoMode && !state.isRecording) {
                    VideoFormatPill(
                        state = state,
                        compact = compact,
                        onCycleQuality = onCycleVideoQuality,
                        onToggleFps = onToggleVideoFps
                    )
                }
                if (state.showsMotion && !state.isRecording) {
                    GlassIconButton(
                        icon = if (state.motionPhotoEnabled) {
                            Icons.Filled.MotionPhotosOn
                        } else {
                            Icons.Filled.MotionPhotosOff
                        },
                        contentDescription = stringResource(
                            if (state.motionPhotoEnabled) R.string.motion_photo_on else R.string.motion_photo_off
                        ),
                        selected = state.motionPhotoEnabled,
                        compact = compact,
                        tooltip = true,
                        onClick = onMotionClicked
                    )
                }
                if (state.allowsEffect && state.effect != EffectMode.NONE && !state.isRecording) {
                    Box(
                        modifier = Modifier
                            .size(controlSize)
                            .clip(CircleShape)
                            .background(CameraAccent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = onStylesToggle
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        FilterVennIcon(
                            modifier = Modifier.size(20.dp),
                            tint = Color.Black
                        )
                    }
                }
                AnimatedVisibility(
                    visible = !state.isRecording && !state.panoramaActive,
                    enter = fadeIn(tween(280)),
                    exit = fadeOut(tween(220))
                ) {
                    GlassIconButton(
                        icon = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.settings),
                        compact = compact,
                        tooltip = true,
                        onGlass = chrome?.onGlass,
                        glass = chrome?.glass,
                        stroke = chrome?.stroke,
                        onClick = onSettingsClicked
                    )
                }
            }
        }
        if (exposureOpen && state.showsExposureControls) {
            ExposureControls(
                state = state,
                compact = compact,
                onPrioritySelected = onExposurePrioritySelected,
                onIsoChanged = onIsoChanged,
                onShutterChanged = onShutterChanged,
                onCompensationChanged = onCompensationChanged
            )
        }
    }
}

@Composable
fun RecordingHud(
    state: CameraUiState,
    compact: Boolean = false,
    onPauseClicked: () -> Unit,
    onMuteClicked: () -> Unit
) {
    AnimatedVisibility(
        visible = state.isRecording,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val pulse by rememberInfiniteTransition(label = "rec").animateFloat(
            initialValue = 0.55f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
            label = "recPulse"
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(CameraGlassStrong)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Canvas(Modifier.size(8.dp)) {
                    drawCircle(CameraDanger.copy(alpha = if (state.isPaused) 0.4f else pulse))
                }
                Text(
                    text = if (state.isPaused) {
                        stringResource(R.string.recording_paused)
                    } else {
                        val time = formatRecordingTime(state.recordingNanos)
                        if (state.recordingSizeBytes > 0L) {
                            "$time · ${formatFileSize(state.recordingSizeBytes)}"
                        } else {
                            time
                        }
                    },
                    color = CameraOnGlass,
                    fontFamily = CameraMono,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(CameraGlassStrong)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                GlassIconButton(
                    icon = if (state.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    contentDescription = stringResource(
                        if (state.isPaused) R.string.resume_recording else R.string.pause_recording
                    ),
                    selected = state.isPaused,
                    compact = compact,
                    embedded = true,
                    tooltip = true,
                    onClick = onPauseClicked
                )
                if (state.showsAudioMuteControl) {
                    val micMuted = state.isMuted || !state.microphonePermissionGranted
                    GlassIconButton(
                        icon = if (micMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                        contentDescription = stringResource(
                            when {
                                !state.microphonePermissionGranted -> R.string.permission_mic_enable
                                micMuted -> R.string.unmute_audio
                                else -> R.string.mute_audio
                            }
                        ),
                        selected = micMuted,
                        compact = compact,
                        embedded = true,
                        tooltip = true,
                        onClick = onMuteClicked
                    )
                }
            }
        }
    }
}

@Composable
fun LiveStatusStrip(state: CameraUiState) {
    val chips = buildList {
        if (state.showsStillBadge) {
            add(
                when {
                    state.stillFormat == StillFormat.RAW_JPEG -> stringResource(R.string.raw_dng)
                    state.stillFormat == StillFormat.HEIC_ULTRA_HDR ->
                        stringResource(R.string.ultrahdr_heic)
                    else -> stringResource(R.string.ultrahdr)
                }
            )
        }
        if (state.showsNightHint && state.autoNightActive) add(stringResource(R.string.night_mode_active))
        else if (state.showsNightHint && state.nightScene == NightScene.RECOMMENDED) {
            add(stringResource(R.string.night_mode_recommended))
        }
        if (state.showsPip) add(stringResource(R.string.dual_live_hint))
        if (state.captureAction == CaptureAction.PANORAMA) {
            add(
                if (state.panoramaActive) {
                    stringResource(R.string.panorama_panning, state.panoramaFrames.size)
                } else {
                    stringResource(R.string.panorama_hint)
                }
            )
        }
        if (state.showsSlowMotionFps &&
            state.slowMotionSupported &&
            state.slowMotionFps > 0
        ) {
            add(stringResource(R.string.slow_motion_fps, state.slowMotionFps))
        }
        if (state.showsVideoStatus && state.videoStabilizationActive) {
            add(stringResource(R.string.video_stabilization_on))
        }
        when (state.videoHdrBound) {
            VideoHdrRange.HLG10 ->
                if (state.showsVideoStatus) add(stringResource(R.string.video_hdr_hlg))
            VideoHdrRange.HDR10 ->
                if (state.showsVideoStatus) add(stringResource(R.string.video_hdr_hdr10))
            VideoHdrRange.HDR10_PLUS ->
                if (state.showsVideoStatus) add(stringResource(R.string.video_hdr_hdr10_plus))
            VideoHdrRange.DOLBY_VISION ->
                if (state.showsVideoStatus) add(stringResource(R.string.video_hdr_dolby))
            VideoHdrRange.SDR -> Unit
        }
        if (state.videoFps60Active && state.showsVideoStatus) {
            add(stringResource(R.string.video_fps_60_on))
        }
        if (state.lowLightBoostActive && state.showsLowLightBoost) {
            add(stringResource(R.string.low_light_boost_on))
        }
    }
    AnimatedVisibility(
        visible = chips.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .horizontalScroll(rememberScrollState())
                .clip(RoundedCornerShape(20.dp))
                .background(CameraGlassStrong)
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            chips.forEach { label ->
                Text(
                    text = label,
                    color = CameraAccent,
                    fontFamily = CameraMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ExposureControls(
    state: CameraUiState,
    compact: Boolean = false,
    onPrioritySelected: (ExposurePriority) -> Unit,
    onIsoChanged: (Int) -> Unit,
    onShutterChanged: (Long) -> Unit,
    onCompensationChanged: (Int) -> Unit
) {
    val limits = state.exposureLimits
    val showHybrid = limits.supportedPriorities.size >= 2
    val showEv = limits.evSupported && limits.evMax > limits.evMin
    if (!showHybrid && !showEv) return

    val panelShape = RoundedCornerShape(18.dp)
    val sliderColors = SliderDefaults.colors(
        thumbColor = CameraAccent,
        activeTrackColor = CameraAccent,
        inactiveTrackColor = Color.White.copy(alpha = 0.18f),
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent
    )
    val horizontalPad = if (compact) 12.dp else 14.dp
    val verticalPad = if (compact) 10.dp else 12.dp

    Column(
        modifier = Modifier
            .fillMaxWidth(if (compact) 0.92f else 0.86f)
            .padding(top = 8.dp)
            .clip(panelShape)
            .background(CameraGlassStrong)
            .border(1.dp, Color.White.copy(alpha = 0.12f), panelShape)
            .padding(horizontal = horizontalPad, vertical = verticalPad),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.exposure_panel_title).uppercase(),
            color = CameraOnGlassMuted,
            fontFamily = CameraMono,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            letterSpacing = 1.2.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        if (showHybrid) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            ) {
                limits.supportedPriorities.forEach { priority ->
                    val selected = state.exposurePriority == priority
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) CameraAccent.copy(alpha = 0.92f) else Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { onPrioritySelected(priority) }
                            )
                            .padding(vertical = if (compact) 8.dp else 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(priority.labelRes),
                            color = if (selected) Color.Black else CameraOnGlass,
                            fontFamily = CameraMono,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (compact) 11.sp else 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = state.exposurePriority == ExposurePriority.ISO,
                enter = fadeIn(tween(160)) + expandVertically(tween(180)),
                exit = fadeOut(tween(120)) + shrinkVertically(tween(140))
            ) {
                ExposureMeterRow(
                    label = stringResource(R.string.ae_iso),
                    value = stringResource(R.string.iso_value, state.iso),
                    compact = compact
                ) {
                    Slider(
                        value = state.iso.toFloat(),
                        onValueChange = { onIsoChanged(it.toInt()) },
                        valueRange = limits.isoMin.toFloat()..limits.isoMax.toFloat(),
                        colors = sliderColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            AnimatedVisibility(
                visible = state.exposurePriority == ExposurePriority.SHUTTER,
                enter = fadeIn(tween(160)) + expandVertically(tween(180)),
                exit = fadeOut(tween(120)) + shrinkVertically(tween(140))
            ) {
                ExposureMeterRow(
                    label = stringResource(R.string.ae_shutter),
                    value = shutterLabel(state.shutterNanos),
                    compact = compact
                ) {
                    Slider(
                        value = state.shutterNanos.toFloat(),
                        onValueChange = { onShutterChanged(it.toLong()) },
                        valueRange = limits.shutterMinNanos.toFloat()..limits.shutterMaxNanos.toFloat(),
                        colors = sliderColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (showEv) {
            val ev = state.exposureCompensation * limits.evStep
            val evLabel = when {
                ev > 0.05f -> "+%.1f".format(ev)
                ev < -0.05f -> "%.1f".format(ev)
                else -> "0.0"
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.exposure_ev_minus),
                        color = CameraOnGlassMuted,
                        fontFamily = CameraMono,
                        fontSize = 14.sp,
                        modifier = Modifier.width(18.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.exposure_ev, evLabel),
                        color = if (state.exposureCompensation == 0) CameraOnGlass else CameraAccent,
                        fontFamily = CameraMono,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = if (compact) 12.sp else 13.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.exposure_ev_plus),
                        color = CameraOnGlassMuted,
                        fontFamily = CameraMono,
                        fontSize = 14.sp,
                        modifier = Modifier.width(18.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Slider(
                    value = state.exposureCompensation.toFloat(),
                    onValueChange = { onCompensationChanged(it.toInt()) },
                    valueRange = limits.evMin.toFloat()..limits.evMax.toFloat(),
                    steps = (limits.evMax - limits.evMin - 1).coerceAtLeast(0),
                    colors = sliderColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ExposureMeterRow(
    label: String,
    value: String,
    compact: Boolean,
    slider: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = CameraOnGlassMuted,
                fontFamily = CameraMono,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = value,
                color = CameraAccent,
                fontFamily = CameraMono,
                fontWeight = FontWeight.SemiBold,
                fontSize = if (compact) 12.sp else 13.sp
            )
        }
        slider()
    }
}

private fun shutterLabel(nanos: Long): String {
    if (nanos <= 0L) return "—"
    val seconds = nanos / 1_000_000_000.0
    return if (seconds >= 1.0) {
        String.format("%.1fs", seconds)
    } else {
        "1/%d".format((1.0 / seconds).toInt().coerceAtLeast(1))
    }
}


/**
 * Quintessential Apple iOS 3-circle Venn diagram filter icon.
 */
@Composable
fun FilterVennIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val r = size.minDimension * 0.28f
        val stroke = Stroke(width = 1.6.dp.toPx())
        val cx = size.width / 2f
        val cy = size.height / 2f
        val offset = r * 0.55f

        // Top circle
        drawCircle(
            color = tint,
            radius = r,
            center = Offset(cx, cy - offset),
            style = stroke
        )
        // Bottom-left circle
        drawCircle(
            color = tint,
            radius = r,
            center = Offset(cx - offset * 0.866f, cy + offset * 0.5f),
            style = stroke
        )
        // Bottom-right circle
        drawCircle(
            color = tint,
            radius = r,
            center = Offset(cx + offset * 0.866f, cy + offset * 0.5f),
            style = stroke
        )
    }
}

@Composable
fun SecondaryToolTray(
    state: CameraUiState,
    compact: Boolean = false,
    onFlashClicked: () -> Unit,
    onTimerClicked: () -> Unit,
    onGridClicked: () -> Unit,
    onMotionClicked: () -> Unit,
    onExposureClicked: () -> Unit,
    isExposureOpen: Boolean,
    onStylesClicked: () -> Unit = {},
    isStylesOpen: Boolean = false,
    onAspectClicked: () -> Unit = {},
    isAspectOpen: Boolean = false,
    onNightClicked: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .background(CameraGlassStrong)
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (state.showsFlash) {
            GlassIconButton(
                icon = flashIcon(state.flash),
                contentDescription = stringResource(state.flash.labelRes),
                selected = state.flash != FlashMode.OFF,
                compact = compact,
                embedded = true,
                onClick = onFlashClicked
            )
        }
        val showNightInTray = state.showsNightHint &&
            CameraExtension.NIGHT in state.supportedExtensions &&
            !state.isRecording
        if (showNightInTray) {
            val nightActive = state.extension == CameraExtension.NIGHT || state.autoNightActive
            Box(
                modifier = Modifier
                    .size(if (compact) 40.dp else 44.dp)
                    .clip(CircleShape)
                    .background(
                        if (nightActive) CameraAccent else Color.White.copy(alpha = 0.08f)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onNightClicked
                    ),
                contentAlignment = Alignment.Center
            ) {
                NightModeMoonIcon(
                    modifier = Modifier.size(18.dp),
                    tint = if (nightActive) Color.Black else CameraOnGlass
                )
            }
        }
        if (state.showsTimer) {
            GlassIconButton(
                icon = timerIcon(state.timer),
                contentDescription = stringResource(state.timer.labelRes),
                selected = state.timer != TimerMode.OFF,
                compact = compact,
                embedded = true,
                onClick = onTimerClicked
            )
        }
        if (state.showsExposureControls) {
            val exposureActive = isExposureOpen ||
                state.exposurePriority != ExposurePriority.AUTO ||
                state.exposureCompensation != 0
            GlassIconButton(
                icon = Icons.Filled.Exposure,
                contentDescription = stringResource(R.string.exposure_button),
                selected = exposureActive,
                compact = compact,
                embedded = true,
                onClick = onExposureClicked
            )
        }
        if (state.showsMotion) {
            GlassIconButton(
                icon = if (state.motionPhotoEnabled) {
                    Icons.Filled.MotionPhotosOn
                } else {
                    Icons.Filled.MotionPhotosOff
                },
                contentDescription = stringResource(
                    if (state.motionPhotoEnabled) R.string.motion_photo_on else R.string.motion_photo_off
                ),
                selected = state.motionPhotoEnabled,
                compact = compact,
                embedded = true,
                onClick = onMotionClicked
            )
        }
        if (state.showsGrid) {
            GlassIconButton(
                icon = if (state.gridEnabled) Icons.Filled.GridOn else Icons.Filled.GridOff,
                contentDescription = stringResource(
                    if (state.gridEnabled) R.string.grid_on else R.string.grid_off
                ),
                selected = state.gridEnabled,
                compact = compact,
                embedded = true,
                onClick = onGridClicked
            )
        }
        if (state.allowsEffect) {
            val styleActive = state.effect != EffectMode.NONE || isStylesOpen
            Box(
                modifier = Modifier
                    .size(if (compact) 40.dp else 44.dp)
                    .clip(CircleShape)
                    .background(
                        if (styleActive) CameraAccent else Color.White.copy(alpha = 0.08f)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onStylesClicked
                    ),
                contentAlignment = Alignment.Center
            ) {
                FilterVennIcon(
                    modifier = Modifier.size(20.dp),
                    tint = if (styleActive) Color.Black else CameraOnGlass
                )
            }
        }
        if (state.showsAspectControl) {
            val aspectActive = isAspectOpen || state.captureAspect != CaptureAspect.RATIO_4_3
            Box(
                modifier = Modifier
                    .size(if (compact) 40.dp else 44.dp)
                    .clip(CircleShape)
                    .background(
                        if (aspectActive) CameraAccent else Color.White.copy(alpha = 0.08f)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onAspectClicked
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.captureAspect.shortLabel,
                    color = if (aspectActive) Color.Black else CameraOnGlass,
                    fontFamily = CameraFontFamily,
                    fontWeight = if (aspectActive) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun ZoomControl(
    state: CameraUiState,
    onZoomSelected: (Float) -> Unit,
    onZoomContinuous: (Float) -> Unit = {}
) {
    if (!state.showsZoomChips || state.zoomChips.size < 2) return

    var isDialExpanded by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableLongStateOf(0L) }

    // Auto-collapse after 2.5s of inactivity
    LaunchedEffect(isDialExpanded, lastInteractionTime) {
        if (isDialExpanded) {
            delay(2500)
            isDialExpanded = false
        }
    }

    // Collapse when recording starts or mode changes
    LaunchedEffect(state.mode, state.isRecording) {
        isDialExpanded = false
    }

    AnimatedVisibility(
        visible = !isDialExpanded,
        enter = fadeIn(tween(180)) + expandVertically(tween(200)),
        exit = fadeOut(tween(140)) + shrinkVertically(tween(160))
    ) {
        ZoomChipsRow(
            state = state,
            onZoomSelected = onZoomSelected,
            onZoomContinuous = onZoomContinuous,
            onExpandDial = {
                lastInteractionTime = System.currentTimeMillis()
                isDialExpanded = true
            }
        )
    }

    AnimatedVisibility(
        visible = isDialExpanded,
        enter = fadeIn(tween(200)) + expandVertically(tween(220)),
        exit = fadeOut(tween(160)) + shrinkVertically(tween(180))
    ) {
        ZoomRulerDial(
            state = state,
            onZoomContinuous = { zoom ->
                lastInteractionTime = System.currentTimeMillis()
                onZoomContinuous(zoom)
            },
            onClose = { isDialExpanded = false }
        )
    }
}

@Composable
fun ZoomChips(
    state: CameraUiState,
    onZoomSelected: (Float) -> Unit
) {
    ZoomControl(state = state, onZoomSelected = onZoomSelected)
}

@Composable
private fun ZoomChipsRow(
    state: CameraUiState,
    onZoomSelected: (Float) -> Unit,
    onZoomContinuous: (Float) -> Unit = {},
    onExpandDial: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val slop = LocalViewConfiguration.current.touchSlop
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(CameraGlassStrong)
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        val dx = change.position.x - down.position.x
                        if (kotlin.math.abs(dx) > slop) {
                            if (change.positionChanged()) change.consume()
                            onExpandDial()
                            break
                        }
                    }
                }
            }
            .padding(horizontal = 4.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        state.zoomChips.forEach { ratio ->
            val isOneX = kotlin.math.abs(ratio - 1.0f) < 0.05f
            val currentZoom = state.zoomRatio
            val isAroundOneX = isOneX && currentZoom in 0.95f..1.55f
            val selected = if (isOneX) isAroundOneX else kotlin.math.abs(state.activeZoomChip - ratio) < 0.12f

            val focalMm = if (isAroundOneX) {
                when {
                    currentZoom in 1.15f..1.35f -> "28"
                    currentZoom in 1.4f..1.6f -> "35"
                    else -> "24"
                }
            } else null

            val label = when {
                ratio < 1f -> {
                    val formatted = String.format(java.util.Locale.US, "%.1f", ratio)
                    if (formatted.startsWith("0.")) ".${formatted.substring(2)}" else formatted
                }
                ratio % 1.0f == 0.0f -> "${ratio.toInt()}"
                else -> String.format(java.util.Locale.US, "%.1f", ratio)
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) Color.White.copy(alpha = 0.18f) else Color.Transparent
                    )
                    .pointerInput(ratio, isOneX, currentZoom, selected) {
                        detectTapGestures(
                            onTap = {
                                if (isOneX && selected) {
                                    val nextRatio = when {
                                        currentZoom < 1.15f -> 1.2f
                                        currentZoom < 1.4f -> 1.5f
                                        else -> 1.0f
                                    }
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onZoomContinuous(nextRatio)
                                } else {
                                    onZoomSelected(ratio)
                                }
                            },
                            onLongPress = { onExpandDial() }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isAroundOneX && selected) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val zoomText = when {
                            currentZoom in 1.15f..1.35f -> "1.2"
                            currentZoom in 1.4f..1.6f -> "1.5"
                            else -> "1"
                        }
                        Text(
                            text = zoomText,
                            color = CameraAccent,
                            fontFamily = CameraMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            lineHeight = 11.sp
                        )
                        Text(
                            text = "${focalMm}mm",
                            color = CameraAccent.copy(alpha = 0.85f),
                            fontFamily = CameraMono,
                            fontWeight = FontWeight.Medium,
                            fontSize = 7.sp,
                            lineHeight = 7.sp
                        )
                    }
                } else {
                    Text(
                        text = label,
                        color = if (selected) CameraAccent else CameraOnGlass,
                        fontFamily = CameraMono,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoomRulerDial(
    state: CameraUiState,
    onZoomContinuous: (Float) -> Unit,
    onClose: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val minZoom = state.minZoom.coerceAtLeast(0.5f)
    val maxZoom = state.maxZoom.coerceAtLeast(minZoom + 0.1f)
    val currentRatio = state.zoomRatio.coerceIn(minZoom, maxZoom)

    val ticks = remember(minZoom, maxZoom) {
        val list = mutableListOf<Float>()
        var v = 0.5f
        while (v <= 15.0f) {
            if (v in minZoom..maxZoom) {
                list.add(v)
            }
            val step = when {
                v < 1.0f -> 0.1f
                v < 3.0f -> 0.2f
                v < 6.0f -> 0.5f
                else -> 1.0f
            }
            v = ((v + step) * 10f).roundToInt() / 10f
        }
        list
    }
    val majorTicks = remember { setOf(0.5f, 1.0f, 2.0f, 3.0f, 5.0f, 10.0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Zoom multiplier badge (e.g. "2.4×")
        val badgeText = String.format(java.util.Locale.US, "%.1f×", currentRatio)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(CameraGlassStrong)
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true),
                    onClick = onClose
                )
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badgeText,
                color = CameraAccent,
                fontFamily = CameraMono,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(Modifier.height(4.dp))

        // Horizontal Ruler Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(CameraGlassStrong)
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
                .pointerInput(minZoom, maxZoom) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var lastX = down.position.x
                        var lastStep = (state.zoomRatio * 10).toInt()
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            val dx = change.position.x - lastX
                            lastX = change.position.x
                            if (kotlin.math.abs(dx) > 0.01f) {
                                val pxPerLog = 220.dp.toPx()
                                val currentLog = ln(state.zoomRatio.coerceIn(minZoom, maxZoom))
                                val newLog = currentLog - (dx / pxPerLog)
                                val newZoom = exp(newLog).coerceIn(minZoom, maxZoom)
                                onZoomContinuous(newZoom)

                                val step = (newZoom * 10).toInt()
                                if (step != lastStep) {
                                    lastStep = step
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                if (change.positionChanged()) change.consume()
                            }
                        }
                    }
                }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val pxPerLog = 220.dp.toPx()
                val currentLog = ln(currentRatio)

                // Draw tick marks
                ticks.forEach { t ->
                    val offset = (ln(t) - currentLog) * pxPerLog
                    val x = centerX + offset
                    if (x in -10f..(size.width + 10f)) {
                        val distFromEdge = minOf(x, size.width - x).coerceAtLeast(0f)
                        val fadeFactor = (distFromEdge / (size.width * 0.25f)).coerceIn(0f, 1f)
                        val isMajor = majorTicks.any { kotlin.math.abs(it - t) < 0.05f }
                        val tickHeight = if (isMajor) 18.dp.toPx() else 10.dp.toPx()
                        val tickWidth = if (isMajor) 1.8.dp.toPx() else 1.dp.toPx()
                        val tickAlpha = (if (isMajor) 0.9f else 0.35f) * fadeFactor
                        val topY = (size.height - tickHeight) / 2f
                        drawLine(
                            color = Color.White.copy(alpha = tickAlpha),
                            start = Offset(x, topY),
                            end = Offset(x, topY + tickHeight),
                            strokeWidth = tickWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Center indicator needle in CameraAccent
                val needleHeight = 24.dp.toPx()
                val needleTop = (size.height - needleHeight) / 2f
                drawLine(
                    color = CameraAccent,
                    start = Offset(centerX, needleTop),
                    end = Offset(centerX, needleTop + needleHeight),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = CameraAccent,
                    radius = 3.dp.toPx(),
                    center = Offset(centerX, needleTop)
                )
            }
        }
    }
}

@Composable
private fun PhotographicStyleDisc(
    effect: EffectMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.14f else 1.0f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow),
        label = "styleDiscScale"
    )

    val gradient = remember(effect) {
        when (effect) {
            EffectMode.NONE -> Brush.linearGradient(
                listOf(Color(0xFFF2F2F7), Color(0xFFC7C7CC), Color(0xFF8E8E93))
            )
            EffectMode.VIVID -> Brush.linearGradient(
                listOf(Color(0xFFFF375F), Color(0xFFFF9F0A), Color(0xFFFF2D55))
            )
            EffectMode.WARM -> Brush.linearGradient(
                listOf(Color(0xFFFF9F0A), Color(0xFFFFD60A), Color(0xFFFF7A00))
            )
            EffectMode.COOL -> Brush.linearGradient(
                listOf(Color(0xFF0A84FF), Color(0xFF64D2FF), Color(0xFF5E5CE6))
            )
            EffectMode.SEPIA -> Brush.linearGradient(
                listOf(Color(0xFF8E5A2A), Color(0xFFD4A373), Color(0xFF5A3A1A))
            )
            EffectMode.GRAYSCALE -> Brush.linearGradient(
                listOf(Color(0xFFE5E5EA), Color(0xFF8E8E93), Color(0xFF3A3A3C))
            )
            EffectMode.INVERT -> Brush.linearGradient(
                listOf(Color.White, Color(0xFF1C1C1E))
            )
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = false),
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .size(50.dp)
                .then(
                    if (selected) {
                        Modifier
                            .border(2.dp, CameraAccent, CircleShape)
                            .padding(2.5.dp)
                    } else {
                        Modifier.padding(2.5.dp)
                    }
                )
                .clip(CircleShape)
                .background(gradient)
                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (effect == EffectMode.NONE) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = 2.5.dp.toPx()
                    )
                }
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text = stringResource(effect.styleTitleRes),
            color = if (selected) CameraAccent else Color.White.copy(alpha = 0.65f),
            fontFamily = CameraFontFamily,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PhotographicStylesTray(
    state: CameraUiState,
    visible: Boolean,
    onEffectSelected: (EffectMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    AnimatedVisibility(
        visible = visible && !state.isRecording && !state.panoramaActive,
        enter = fadeIn(tween(220)) + expandVertically(tween(240)),
        exit = fadeOut(tween(180)) + shrinkVertically(tween(200)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top: Active Style Title & Reset Pill
            Row(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CameraGlassStrong)
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(state.effect.styleTitleRes).uppercase(),
                    color = if (state.effect != EffectMode.NONE) CameraAccent else Color.White,
                    fontFamily = CameraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.2.sp
                )
                if (state.effect != EffectMode.NONE) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onEffectSelected(EffectMode.NONE)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.style_reset),
                            tint = CameraAccent,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Carousel of Style Discs
            val itemWidth = 68.dp
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val edgeInset = ((maxWidth - itemWidth) / 2).coerceAtLeast(0.dp)
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier.horizontalScroll(scrollState),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(edgeInset))
                    EffectMode.entries.forEachIndexed { index, effect ->
                        if (index > 0) Spacer(Modifier.width(10.dp))
                        PhotographicStyleDisc(
                            effect = effect,
                            selected = state.effect == effect,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onEffectSelected(effect)
                            },
                            modifier = Modifier.width(itemWidth)
                        )
                    }
                    Spacer(Modifier.width(edgeInset))
                }
            }
        }
    }
}

@Composable
fun AspectRatioTray(
    state: CameraUiState,
    visible: Boolean,
    onAspectSelected: (CaptureAspect) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    AnimatedVisibility(
        visible = visible && !state.isRecording && !state.panoramaActive,
        enter = fadeIn(tween(220)) + expandVertically(tween(240)),
        exit = fadeOut(tween(180)) + shrinkVertically(tween(200)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(CameraGlassStrong)
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CaptureAspect.entries.forEach { aspect ->
                    val selected = state.captureAspect == aspect
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.05f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.72f),
                        label = "aspectScale"
                    )
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) CameraAccent else Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onAspectSelected(aspect)
                                }
                            )
                            .padding(horizontal = 18.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = aspect.shortLabel,
                            color = if (selected) Color.Black else Color.White.copy(alpha = 0.75f),
                            fontFamily = CameraFontFamily,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EffectsFilmstrip(
    state: CameraUiState,
    onEffectSelected: (EffectMode) -> Unit
) {
    PhotographicStylesTray(
        state = state,
        visible = state.showsEffects,
        onEffectSelected = onEffectSelected
    )
}

private fun cameraFooterBottomPadding(compact: Boolean) = if (compact) 8.dp else 14.dp

private fun modePagerHeight(compact: Boolean) = if (compact) 32.dp else 40.dp

private fun modePagerAfterGap(compact: Boolean) = if (compact) 8.dp else 10.dp

/** Pager row plus footer padding; pair with bottom [WindowInsets] on Others. */
internal fun othersFooterContentClearance(compact: Boolean): Dp =
    modePagerHeight(compact) + modePagerAfterGap(compact) +
        cameraFooterBottomPadding(compact) + 12.dp

@Composable
fun CameraFooter(
    state: CameraUiState,
    compact: Boolean = false,
    drawerOpen: Boolean = false,
    stylesOpen: Boolean = false,
    onStylesToggle: () -> Unit = {},
    aspectOpen: Boolean = false,
    onAspectToggle: () -> Unit = {},
    onAspectSelected: (CaptureAspect) -> Unit = {},
    onNightClicked: () -> Unit = {},
    onModeSelected: (CameraMode) -> Unit,
    onFlipClicked: () -> Unit,
    onShutterClicked: () -> Unit,
    onGalleryClicked: () -> Unit,
    onEffectSelected: (EffectMode) -> Unit,
    onZoomSelected: (Float) -> Unit = {},
    onZoomContinuous: (Float) -> Unit = {},
    onQuickTakeStart: () -> Unit = {},
    onQuickTakeStop: () -> Unit = {},
    onFlashClicked: () -> Unit = {},
    onTimerClicked: () -> Unit = {},
    onGridClicked: () -> Unit = {},
    onMotionClicked: () -> Unit = {},
    onExposurePrioritySelected: (ExposurePriority) -> Unit = {},
    onIsoChanged: (Int) -> Unit = {},
    onShutterChanged: (Long) -> Unit = {},
    onCompensationChanged: (Int) -> Unit = {}
) {
    var footerExposureOpen by remember { mutableStateOf(false) }
    var isQuickTaking by remember { mutableStateOf(false) }
    var isQuickTakeLocked by remember { mutableStateOf(false) }
    var quickTakeDragOffset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(state.isRecording) {
        if (!state.isRecording) {
            isQuickTaking = false
            isQuickTakeLocked = false
            quickTakeDragOffset = 0f
        }
    }
    LaunchedEffect(drawerOpen) {
        if (!drawerOpen) footerExposureOpen = false
    }
    val chrome = if (state.showsTools) themedOverlayChrome() else null
    val idleModeColor = chrome?.muted ?: Color.White.copy(alpha = 0.65f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    if (chrome != null) {
                        listOf(Color.Transparent, chrome.canvas)
                    } else {
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.42f))
                    }
                )
            )
            .windowInsetsPadding(
                WindowInsets.safeDrawing
                    .union(WindowInsets.systemGestures)
                    .only(WindowInsetsSides.Bottom)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .padding(bottom = cameraFooterBottomPadding(compact)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = drawerOpen && !state.isRecording && !state.panoramaActive,
            enter = fadeIn(tween(220)) + expandVertically(tween(240)),
            exit = fadeOut(tween(180)) + shrinkVertically(tween(200))
        ) {
            Column(
                modifier = Modifier.padding(bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SecondaryToolTray(
                    state = state,
                    compact = compact,
                    onFlashClicked = onFlashClicked,
                    onTimerClicked = onTimerClicked,
                    onGridClicked = onGridClicked,
                    onMotionClicked = onMotionClicked,
                    onExposureClicked = { footerExposureOpen = !footerExposureOpen },
                    isExposureOpen = footerExposureOpen,
                    onStylesClicked = onStylesToggle,
                    isStylesOpen = stylesOpen,
                    onAspectClicked = onAspectToggle,
                    isAspectOpen = aspectOpen,
                    onNightClicked = onNightClicked
                )
                if (footerExposureOpen && state.showsExposureControls) {
                    ExposureControls(
                        state = state,
                        compact = compact,
                        onPrioritySelected = onExposurePrioritySelected,
                        onIsoChanged = onIsoChanged,
                        onShutterChanged = onShutterChanged,
                        onCompensationChanged = onCompensationChanged
                    )
                }
            }
        }
        ZoomControl(
            state = state,
            onZoomSelected = onZoomSelected,
            onZoomContinuous = onZoomContinuous
        )
        if (state.showsZoomChips && state.zoomChips.size >= 2) {
            Spacer(Modifier.height(10.dp))
        }
        if (!state.lockCaptureMode) {
            AspectRatioTray(
                state = state,
                visible = aspectOpen,
                onAspectSelected = onAspectSelected
            )
            PhotographicStylesTray(
                state = state,
                visible = state.showsEffects || stylesOpen,
                onEffectSelected = onEffectSelected
            )
            AnimatedVisibility(
                visible = !state.isRecording && !state.panoramaActive,
                enter = fadeIn(tween(280)) + expandVertically(tween(320)),
                exit = fadeOut(tween(220)) + shrinkVertically(tween(280))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val modes = state.visibleModes
                    key(state.slowMotionSupported, state.concurrentSupported) {
                        DiscretePager(
                            items = modes,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(modePagerHeight(compact)),
                            itemWidth = 96.dp,
                            itemSpacing = 0.dp,
                            overshootFraction = 0.55f,
                            initialIndex = modes.indexOf(state.mode).coerceAtLeast(0),
                            onItemSelected = onModeSelected
                        ) { item, selected ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(item.labelRes).uppercase(),
                                    color = if (selected) CameraAccent else idleModeColor,
                                    fontFamily = CameraFontFamily,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = if (compact) 12.sp else 13.sp,
                                    letterSpacing = 1.0.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(modePagerAfterGap(compact)))
                }
            }
        }
        AnimatedVisibility(
            visible = state.showsCaptureControls,
            enter = fadeIn(tween(280)) + expandVertically(tween(320)),
            exit = fadeOut(tween(220)) + shrinkVertically(tween(280))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                            .union(WindowInsets.systemGestures)
                            .only(WindowInsetsSides.Horizontal)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sideSize = if (compact) 44.dp else 48.dp
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier.size(sideSize),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !state.lockCaptureMode &&
                            !state.isRecording &&
                            !state.panoramaActive,
                        enter = fadeIn(tween(280)),
                        exit = fadeOut(tween(220))
                    ) {
                        GalleryThumb(
                            file = state.thumbnail,
                            size = sideSize,
                            onClick = onGalleryClicked
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                ShutterButton(
                    recordsVideo = state.recordsVideo,
                    isRecording = state.isRecording,
                    panoramaActive = state.panoramaActive,
                    compact = compact,
                    enabled = state.isCameraReady || state.isRecording || state.panoramaActive,
                    isQuickTaking = isQuickTaking,
                    quickTakeDragX = quickTakeDragOffset,
                    onQuickTakeStart = {
                        isQuickTaking = true
                        onQuickTakeStart()
                    },
                    onQuickTakeDrag = { dragX ->
                        quickTakeDragOffset = dragX
                    },
                    onQuickTakeLock = {
                        isQuickTakeLocked = true
                        isQuickTaking = false
                    },
                    onQuickTakeStop = {
                        isQuickTaking = false
                        onQuickTakeStop()
                    },
                    onClick = onShutterClicked
                )
                Spacer(Modifier.weight(1f))
                val lockThresholdPx = with(LocalDensity.current) { 60.dp.toPx() }
                val isNearLock = quickTakeDragOffset >= lockThresholdPx * 0.65f
                if (isQuickTaking && !isQuickTakeLocked) {
                    val lockScale by animateFloatAsState(
                        targetValue = if (isNearLock) 1.18f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "lockScale"
                    )
                    Box(
                        modifier = Modifier
                            .size(sideSize)
                            .graphicsLayer {
                                scaleX = lockScale
                                scaleY = lockScale
                            }
                            .clip(CircleShape)
                            .background(if (isNearLock) CameraAccent else CameraGlassStrong)
                            .border(
                                1.5.dp,
                                if (isNearLock) CameraAccent else Color.White.copy(alpha = 0.35f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isNearLock) Icons.Filled.Lock else Icons.Filled.LockOpen,
                            contentDescription = stringResource(R.string.quicktake_lock),
                            tint = if (isNearLock) Color.Black else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else if (state.showsFlipControl) {
                    var flipRotation by remember { mutableFloatStateOf(0f) }
                    val animatedFlipRotation by animateFloatAsState(
                        targetValue = flipRotation,
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMediumLow,
                            dampingRatio = 0.72f
                        ),
                        label = "flipCameraRotation"
                    )
                    Box(
                        modifier = Modifier
                            .size(sideSize)
                            .graphicsLayer {
                                rotationY = animatedFlipRotation
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        GlassIconButton(
                            icon = Icons.Filled.Cameraswitch,
                            contentDescription = stringResource(R.string.switch_camera_button_alt),
                            diameter = sideSize,
                            onClick = {
                                flipRotation += 180f
                                onFlipClicked()
                            }
                        )
                    }
                } else {
                    Spacer(Modifier.size(sideSize))
                }
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ShutterButton(
    recordsVideo: Boolean,
    isRecording: Boolean,
    panoramaActive: Boolean = false,
    compact: Boolean = false,
    enabled: Boolean = true,
    isQuickTaking: Boolean = false,
    quickTakeDragX: Float = 0f,
    onQuickTakeStart: () -> Unit = {},
    onQuickTakeDrag: (Float) -> Unit = {},
    onQuickTakeLock: () -> Unit = {},
    onQuickTakeStop: () -> Unit = {},
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isPressedLocal by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = when {
            isPressedLocal -> 0.88f
            isQuickTaking -> 1.05f
            else -> 1f
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "shutterPressScale"
    )
    val innerScale by animateFloatAsState(
        targetValue = if (isRecording || panoramaActive || isQuickTaking) 0.44f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "shutterScale"
    )
    val corner by animateFloatAsState(
        targetValue = if ((isRecording || panoramaActive) && !isQuickTaking) 0.22f else 0.5f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "shutterCorner"
    )
    val alpha by animateFloatAsState(
        if (enabled) 1f else 0.45f,
        label = "shutterAlpha"
    )
    val buttonSize = if (compact) 68.dp else 84.dp
    val density = LocalDensity.current
    val lockThresholdPx = with(density) { 60.dp.toPx() }

    Box(
        modifier = Modifier
            .size(buttonSize)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .alpha(alpha)
            .pointerInput(enabled, recordsVideo, isRecording, panoramaActive) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isPressedLocal = true
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val startPos = down.position

                    if (isRecording || recordsVideo || panoramaActive) {
                        val up = waitForUpOrCancellation()
                        isPressedLocal = false
                        if (up != null) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onClick()
                        }
                        return@awaitEachGesture
                    }

                    // Photo mode: QuickTake support
                    val upBeforeTimeout = try {
                        withTimeout(350L) {
                            waitForUpOrCancellation()
                        }
                    } catch (e: PointerEventTimeoutCancellationException) {
                        null
                    }
                    if (upBeforeTimeout != null) {
                        // Released quickly: standard photo capture!
                        isPressedLocal = false
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                        return@awaitEachGesture
                    }

                    // Held for 350ms: QuickTake video recording starts!
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onQuickTakeStart()

                    var locked = false
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            break
                        }
                        val dx = (change.position.x - startPos.x).coerceAtLeast(0f)
                        onQuickTakeDrag(dx)
                        if (dx >= lockThresholdPx) {
                            locked = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onQuickTakeLock()
                            break
                        }
                    }
                    isPressedLocal = false
                    if (!locked) {
                        onQuickTakeStop()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val strokeWidth = 3.5.dp.toPx()
            val ringGap = 3.5.dp.toPx()
            val outerRadius = (size.minDimension - strokeWidth) / 2f

            // Outer crisp white border ring
            drawCircle(
                color = Color.White,
                style = Stroke(width = strokeWidth),
                radius = outerRadius
            )

            // Inner tactile disc
            val maxInnerDiameter = size.minDimension - (strokeWidth * 2 + ringGap * 2)
            val currentInner = maxInnerDiameter * innerScale
            val dragOffsetPx = if (isQuickTaking) {
                (quickTakeDragX * 0.35f).coerceAtMost(28.dp.toPx())
            } else {
                0f
            }
            val origin = Offset(
                x = (size.width - currentInner) / 2f + dragOffsetPx,
                y = (size.height - currentInner) / 2f
            )
            val color = when {
                recordsVideo || isRecording || isQuickTaking -> CameraDanger
                panoramaActive -> CameraAccent
                else -> Color.White
            }
            drawRoundRect(
                color = color,
                topLeft = origin,
                size = Size(currentInner, currentInner),
                cornerRadius = CornerRadius(currentInner * corner, currentInner * corner)
            )
        }
    }
}

@Composable
fun RuleOfThirdsGrid() {
    Canvas(Modifier.fillMaxSize()) {
        val color = Color.White.copy(alpha = 0.28f)
        val stroke = 1.dp.toPx()
        drawLine(color, Offset(size.width / 3f, 0f), Offset(size.width / 3f, size.height), stroke)
        drawLine(
            color,
            Offset(size.width * 2f / 3f, 0f),
            Offset(size.width * 2f / 3f, size.height),
            stroke
        )
        drawLine(color, Offset(0f, size.height / 3f), Offset(size.width, size.height / 3f), stroke)
        drawLine(
            color,
            Offset(0f, size.height * 2f / 3f),
            Offset(size.width, size.height * 2f / 3f),
            stroke
        )
    }
}

@Composable
fun FocusRing(
    point: Offset?,
    exposureIndex: Int = 0,
    evMin: Int = -4,
    evMax: Int = 4
) {
    AnimatedVisibility(
        visible = point != null,
        enter = fadeIn(tween(120)),
        exit = fadeOut(tween(300))
    ) {
        if (point == null) return@AnimatedVisibility
        Canvas(Modifier.fillMaxSize()) {
            val boxSize = 64.dp.toPx()
            val left = point.x - boxSize / 2f
            val top = point.y - boxSize / 2f

            // Draw classic iOS yellow focus square
            drawRoundRect(
                color = CameraAccent,
                topLeft = Offset(left, top),
                size = Size(boxSize, boxSize),
                cornerRadius = CornerRadius(4.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Vertical exposure adjustment line
            val sunCenterX = left + boxSize + 16.dp.toPx()
            val sliderLineHalf = 28.dp.toPx()
            drawLine(
                color = CameraAccent.copy(alpha = 0.35f),
                start = Offset(sunCenterX, top + boxSize / 2f - sliderLineHalf),
                end = Offset(sunCenterX, top + boxSize / 2f + sliderLineHalf),
                strokeWidth = 1.dp.toPx()
            )

            // Sun position on vertical line based on exposure compensation
            val evSpan = (evMax - evMin).takeIf { it > 0 } ?: 1
            val normalizedEv = ((exposureIndex - evMin).toFloat() / evSpan).coerceIn(0f, 1f)
            val sunCenterY = top + boxSize / 2f + (0.5f - normalizedEv) * (sliderLineHalf * 2)
            val sunRadius = 4.5.dp.toPx()
            val rayLength = 3.dp.toPx()

            // Sun center ring
            drawCircle(
                color = CameraAccent,
                radius = sunRadius,
                center = Offset(sunCenterX, sunCenterY),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 8 sun rays
            for (i in 0 until 8) {
                val angle = (i * 45.0) * Math.PI / 180.0
                val startDist = sunRadius + 2.dp.toPx()
                val endDist = startDist + rayLength
                val startX = (sunCenterX + startDist * Math.cos(angle)).toFloat()
                val startY = (sunCenterY + startDist * Math.sin(angle)).toFloat()
                val endX = (sunCenterX + endDist * Math.cos(angle)).toFloat()
                val endY = (sunCenterY + endDist * Math.sin(angle)).toFloat()
                drawLine(
                    color = CameraAccent,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun CountdownOverlay(value: Int?) {
    if (value == null) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.toString(),
            color = Color.White,
            fontFamily = CameraFontFamily,
            fontSize = 96.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GalleryThumb(
    file: File?,
    size: Dp,
    onClick: () -> Unit
) {
    var model by remember(file) { mutableStateOf<Any?>(null) }
    LaunchedEffect(file) {
        model = withContext(Dispatchers.IO) {
            file?.let { mediaThumbnail(it) }
        }
    }
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(CameraGlassStrong)
            .border(1.5.dp, Color.White.copy(alpha = 0.6f), shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (model != null) {
            AsyncImage(
                model = model,
                contentDescription = stringResource(R.string.gallery_button_alt),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Photo,
                contentDescription = stringResource(R.string.gallery_button_alt),
                tint = CameraOnGlass,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EffectThumb(
    effect: EffectMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fill = when (effect) {
        EffectMode.NONE -> Color.White
        EffectMode.GRAYSCALE -> Color(0xFFBDBDBD)
        EffectMode.INVERT -> Color(0xFF212121)
        EffectMode.SEPIA -> Color(0xFFD7A86E)
        EffectMode.COOL -> Color(0xFF7EC8E3)
        EffectMode.WARM -> Color(0xFFFFB74D)
        EffectMode.VIVID -> Color(0xFFFF5C8A)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = false),
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(fill)
                .then(
                    if (selected) Modifier.border(2.dp, CameraAccent, CircleShape)
                    else Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                )
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(effect.labelRes),
            color = if (selected) CameraAccent else CameraOnGlass,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    selected: Boolean = false,
    compact: Boolean = false,
    embedded: Boolean = false,
    tooltip: Boolean = false,
    diameter: Dp? = null,
    onGlass: Color? = null,
    glass: Color? = null,
    stroke: Color? = null
) {
    val background = when {
        selected -> CameraAccent
        embedded -> Color.White.copy(alpha = 0.08f)
        else -> glass ?: CameraGlassStrong
    }
    val iconTint = if (selected) Color.Black else (onGlass ?: CameraOnGlass)
    val borderColor = stroke ?: Color.White.copy(alpha = 0.14f)
    val size = diameter ?: if (compact || embedded) 40.dp else 44.dp
    val interactionSource = remember { MutableInteractionSource() }
    var showTooltip by remember { mutableStateOf(false) }
    var skipClick by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    if (tooltip) {
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collectLatest { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        skipClick = false
                        delay(android.view.ViewConfiguration.getLongPressTimeout().toLong())
                        skipClick = true
                        showTooltip = true
                    }
                    else -> showTooltip = false
                }
            }
        }
    }
    Box(contentAlignment = Alignment.Center) {
        if (!selected && !embedded) {
            CameraGlassButton(
                icon = icon,
                contentDescription = contentDescription,
                onClick = {
                    if (!skipClick) onClick()
                    skipClick = false
                },
                compact = compact,
                diameter = diameter,
                onGlass = onGlass,
                glass = glass,
                stroke = stroke,
                interactionSource = interactionSource
            )
        } else {
            Box(
                modifier = Modifier
                    .then(if (embedded) Modifier else Modifier.minimumInteractiveComponentSize())
                    .size(size)
                    .clip(CircleShape)
                    .background(background)
                    .then(
                        if (embedded) Modifier
                        else Modifier.border(1.dp, borderColor, CircleShape)
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true),
                        onClick = {
                            if (!skipClick) onClick()
                            skipClick = false
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = iconTint,
                    modifier = Modifier.size(
                        if (diameter != null) 22.dp else if (compact || embedded) 18.dp else 20.dp
                    )
                )
            }
        }
        if (tooltip && showTooltip) {
            Popup(
                popupPositionProvider = object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect,
                        windowSize: IntSize,
                        layoutDirection: LayoutDirection,
                        popupContentSize: IntSize
                    ): IntOffset {
                        val gap = with(density) { 8.dp.roundToPx() }
                        val margin = with(density) { 8.dp.roundToPx() }
                        val x = (anchorBounds.left +
                            (anchorBounds.width - popupContentSize.width) / 2)
                            .coerceIn(
                                margin,
                                (windowSize.width - popupContentSize.width - margin)
                                    .coerceAtLeast(margin)
                            )
                        return IntOffset(x, anchorBounds.bottom + gap)
                    }
                },
                properties = PopupProperties(focusable = false, clippingEnabled = false)
            ) {
                Text(
                    text = contentDescription,
                    color = CameraOnGlass,
                    fontFamily = CameraFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.88f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

private fun flashIcon(mode: FlashMode) = when (mode) {
    FlashMode.OFF -> Icons.Filled.FlashOff
    FlashMode.ON -> Icons.Filled.FlashOn
    FlashMode.AUTO -> Icons.Filled.FlashAuto
    FlashMode.TORCH -> Icons.Filled.FlashlightOn
}

private fun timerIcon(mode: TimerMode) = when (mode) {
    TimerMode.OFF -> Icons.Filled.TimerOff
    TimerMode.THREE -> Icons.Filled.Timer3
    TimerMode.TEN -> Icons.Filled.Timer
}

internal fun mediaThumbnail(file: File?): Any {
    if (file == null) return R.drawable.ic_photo
    if (file.extension.lowercase() != "mp4") return file
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(file.absolutePath)
        retriever.getFrameAtTime(0) ?: R.drawable.ic_camera_video
    } catch (_: Exception) {
        R.drawable.ic_camera_video
    } finally {
        retriever.release()
    }
}

@Composable
fun PanoramaLivePreview(state: CameraUiState) {
    AnimatedVisibility(
        visible = state.panoramaActive,
        enter = fadeIn(tween(160)) + expandVertically(tween(180)),
        exit = fadeOut(tween(120)) + shrinkVertically(tween(140))
    ) {
        val scrollState = rememberScrollState()
        LaunchedEffect(state.panoramaFrames.size) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy((-24).dp)
        ) {
            Spacer(modifier = Modifier.width(32.dp))
            state.panoramaFrames.forEach { file ->
                AsyncImage(
                    model = file,
                    contentDescription = null,
                    modifier = Modifier
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
        }
    }
}
