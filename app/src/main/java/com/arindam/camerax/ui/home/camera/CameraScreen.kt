package com.arindam.camerax.ui.home.camera

import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.view.OrientationEventListener
import android.view.Surface
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clipToBounds
import com.arindam.camerax.util.log.Logger
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arindam.camerax.R
import com.arindam.camerax.domain.model.CameraLens
import com.arindam.camerax.domain.model.CameraMode
import com.arindam.camerax.domain.model.CaptureAction
import com.arindam.camerax.domain.model.CaptureAspect
import com.arindam.camerax.domain.model.profile
import kotlin.math.abs
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arindam.camerax.ui.compose.CameraAlertDialog
import com.arindam.camerax.ui.compose.DarkLightPreviews
import com.arindam.camerax.ui.theme.AppTheme
import androidx.compose.material3.MaterialTheme
import java.io.File
import kotlinx.coroutines.delay

/**
 * Compose viewfinder: [PreviewView] plus overlay chrome from [CameraChrome].
 * Gestures (focus / pinch / drag zoom) stay here; capture goes through [CameraViewModel].
 */
@Composable
fun CameraScreen(
    onGalleryClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onExternalCaptureReady: (File) -> Unit,
    onRequestMicrophonePermission: () -> Unit,
    viewModel: CameraViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val inspection = LocalInspectionMode.current
    val configuration = LocalConfiguration.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FIT_CENTER
            isClickable = false
            isFocusable = false
        }
    }

    var keepPreview by remember { mutableStateOf(true) }
    var showActiveCaptureExitDialog by rememberSaveable { mutableStateOf(false) }
    val activeCaptureInProgress = state.isRecording || state.panoramaActive
    BackHandler(enabled = activeCaptureInProgress) {
        showActiveCaptureExitDialog = true
    }
    LaunchedEffect(activeCaptureInProgress) {
        if (!activeCaptureInProgress) showActiveCaptureExitDialog = false
    }
    LaunchedEffect(
        state.bindRevision,
        state.lens,
        state.extension,
        state.mode,
        previewView
    ) {
        if (inspection) return@LaunchedEffect
        if (state.showsTools) {
            delay(OTHERS_ENTER_MILLIS)
            keepPreview = false
            viewModel.unbindPreview()
        } else {
            keepPreview = true
            viewModel.bind(
                lifecycleOwner,
                previewView
            )
            previewView.display?.rotation?.let(viewModel::updateDisplayRotation)
        }
    }
    LaunchedEffect(configuration.orientation, configuration.screenWidthDp) {
        if (!inspection) {
            previewView.display?.rotation?.let(viewModel::updateDisplayRotation)
        }
    }
    LaunchedEffect(state.message, state.messageRes) {
        val text = state.message
            ?: state.messageRes?.let { context.getString(it) }
            ?: return@LaunchedEffect
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        viewModel.consumeMessage()
    }
    LaunchedEffect(viewModel) {
        viewModel.externalCaptureReady.collect(onExternalCaptureReady)
    }

    DisposableEffect(previewView) {
        val displayManager = context.getSystemService(DisplayManager::class.java)
        val listener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) = Unit
            override fun onDisplayRemoved(displayId: Int) = Unit
            override fun onDisplayChanged(displayId: Int) {
                if (previewView.display?.displayId == displayId) {
                    previewView.display?.rotation?.let(viewModel::updateDisplayRotation)
                }
            }
        }
        displayManager?.registerDisplayListener(listener, Handler(Looper.getMainLooper()))
        onDispose { displayManager?.unregisterDisplayListener(listener) }
    }
    DisposableEffect(context) {
        val orientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return
                val rotation = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                viewModel.updateTargetRotation(rotation)
            }
        }
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable()
        }
        onDispose { orientationEventListener.disable() }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) viewModel.onHostStopped()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    DisposableEffect(state.panoramaActive) {
        if (!state.panoramaActive) return@DisposableEffect onDispose { }
        val manager = context.getSystemService(SensorManager::class.java)
        val sensor = manager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: return@DisposableEffect onDispose { }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val rotation = FloatArray(9)
                val orientation = FloatArray(3)
                SensorManager.getRotationMatrixFromVector(rotation, event.values)
                SensorManager.getOrientation(rotation, orientation)
                viewModel.onPanoramaYaw(Math.toDegrees(orientation[0].toDouble()).toFloat())
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { manager.unregisterListener(listener) }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxHeight < 480.dp ||
            configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val density = LocalDensity.current
        var headerHeightPx by remember { mutableIntStateOf(0) }
        var footerHeightPx by remember { mutableIntStateOf(0) }
        val headerHeightForFocus by rememberUpdatedState(headerHeightPx)
        val isVideoSession = state.mode.profile().captureAction == CaptureAction.VIDEO ||
            state.mode.profile().bindSlowMotion ||
            state.mode.profile().bindConcurrent
        val is43 = !isVideoSession && state.captureAspect == CaptureAspect.RATIO_4_3
        val is169 = isVideoSession || state.captureAspect == CaptureAspect.RATIO_16_9

        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val activeAspect = when {
            is43 -> if (isLandscape) 4f / 3f else 3f / 4f
            is169 -> if (isLandscape) 16f / 9f else 9f / 16f
            else -> null
        }

        var previewTopPx by remember { mutableIntStateOf(0) }
        var previewLeftPx by remember { mutableIntStateOf(0) }
        var previewWidthPx by remember { mutableIntStateOf(0) }
        var previewHeightPx by remember { mutableIntStateOf(0) }

        val verticalShift = if (is43 && !isLandscape && headerHeightPx > 0 && footerHeightPx > 0) {
            with(density) { ((headerHeightPx - footerHeightPx) / 2).toDp() }
        } else {
            0.dp
        }

        val viewfinderModifier = if (activeAspect != null) {
            if (isLandscape) {
                Modifier
                    .fillMaxHeight()
                    .aspectRatio(activeAspect, matchHeightConstraintsFirst = true)
            } else {
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(activeAspect)
            }
        } else {
            Modifier.fillMaxSize()
        }

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    if (state.showsTools) {
                        MaterialTheme.colorScheme.background
                    } else {
                        Color.Black
                    }
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = verticalShift),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = viewfinderModifier
                        .animateContentSize(animationSpec = tween(280))
                        .clipToBounds()
                        .onGloballyPositioned {
                            val pos = it.positionInRoot()
                            previewLeftPx = pos.x.toInt()
                            previewTopPx = pos.y.toInt()
                            previewWidthPx = it.size.width
                            previewHeightPx = it.size.height
                            Logger.debug(
                                "CameraScreen",
                                "Viewfinder: is43=$is43 is169=$is169 aspect=$activeAspect " +
                                    "size=${it.size.width}x${it.size.height} top=$previewTopPx left=$previewLeftPx"
                            )
                        }
                ) {
                    if (keepPreview) {
                        if (state.showsEffects) {
                            AndroidView(
                                factory = { previewView },
                                modifier = Modifier.size(1.dp)
                            )
                            val effectFrame = state.effectFrame
                            if (effectFrame != null) {
                                Image(
                                    bitmap = effectFrame,
                                    contentDescription = stringResource(R.string.effect_frame_description),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            scaleX = if (state.lens == CameraLens.FRONT && state.frontMirror) {
                                                -1f
                                            } else {
                                                1f
                                            }
                                        },
                                    contentScale = if (is43 || is169) ContentScale.Fit else ContentScale.Crop
                                )
                            }
                        } else {
                            AndroidView(
                                factory = { previewView },
                                modifier = Modifier.fillMaxSize(),
                                update = { view ->
                                    view.requestLayout()
                                }
                            )
                        }
                    }
                    if (state.gridEnabled) {
                        RuleOfThirdsGrid()
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0f)
                    .then(
                        if (state.showsTools) {
                            Modifier
                        } else {
                            Modifier
                                // Keep focus / pinch / drag off the chrome so zoom chips
                                // stay reliably tappable while recording (footer animates).
                                .padding(
                                    top = with(density) { headerHeightPx.toDp() },
                                    bottom = with(density) { footerHeightPx.toDp() }
                                )
                                .pointerInput(previewView, state.showsZoomChips) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = true)
                                    val start = down.position
                                    val slop = viewConfiguration.touchSlop
                                    var dragged = false
                                    var pinch = false
                                    var cumulativeZoom = 1f
                                    if (state.showsZoomChips) {
                                        viewModel.beginZoomGesture()
                                    }
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val pressed = event.changes.filter { it.pressed }
                                        if (pressed.isEmpty()) break
                                        if (state.showsZoomChips && pressed.size >= 2) {
                                            pinch = true
                                            dragged = true
                                            cumulativeZoom *= event.calculateZoom()
                                            viewModel.zoomByPinch(cumulativeZoom)
                                            pressed.forEach { change ->
                                                if (change.positionChanged()) change.consume()
                                            }
                                        } else if (state.showsZoomChips && !pinch) {
                                            val pointer = pressed.first()
                                            val dx = pointer.position.x - start.x
                                            val dy = pointer.position.y - start.y
                                            if (abs(dx) > slop || abs(dy) > slop) {
                                                if (abs(dy) >= abs(dx)) {
                                                    dragged = true
                                                    viewModel.zoomByDrag(dy, size.height.toFloat())
                                                    if (pointer.positionChanged()) pointer.consume()
                                                } else {
                                                    break
                                                }
                                            }
                                        } else {
                                            val pointer = pressed.first()
                                            val dx = pointer.position.x - start.x
                                            val dy = pointer.position.y - start.y
                                            if (abs(dx) > slop || abs(dy) > slop) dragged = true
                                        }
                                    }
                                    if (!dragged) {
                                        val screenX = start.x
                                        val screenY = start.y + headerHeightForFocus.toFloat()
                                        val localX = screenX - previewLeftPx
                                        val localY = screenY - previewTopPx
                                        val targetWidth = if (previewWidthPx > 0) previewWidthPx else previewView.width
                                        val targetHeight = if (previewHeightPx > 0) previewHeightPx else previewView.height
                                        val clampedX = localX.coerceIn(0f, targetWidth.toFloat())
                                        val clampedY = localY.coerceIn(0f, targetHeight.toFloat())
                                        viewModel.tapToFocus(
                                            previewView = previewView,
                                            localOffset = Offset(clampedX, clampedY),
                                            screenOffset = Offset(screenX, screenY)
                                        )
                                    }
                                }
                            }
                        }
                    )
            )
            FocusRing(state.focusPoint)
            AnimatedVisibility(
                visible = state.showsTools,
                modifier = Modifier.zIndex(0.7f),
                enter = fadeIn(tween(OTHERS_ENTER_MILLIS.toInt())) +
                    slideInVertically(tween(OTHERS_ENTER_MILLIS.toInt())) { distance ->
                        distance / 12
                    },
                exit = fadeOut(tween(260)) +
                    slideOutVertically(tween(260)) { distance ->
                        distance / 14
                    }
            ) {
                OthersWorkspace(
                    state = state,
                    compact = compact,
                    onExit = viewModel::exitTools,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .zIndex(1f)
                    .onGloballyPositioned { headerHeightPx = it.size.height }
            ) {
                CameraHeader(
                    state = state,
                    compact = compact,
                    onFlashClicked = viewModel::cycleFlash,
                    onTimerClicked = viewModel::cycleTimer,
                    onGridClicked = viewModel::toggleGrid,
                    onMotionClicked = viewModel::toggleMotionPhoto,
                    onSettingsClicked = onSettingsClicked,
                    onExposurePrioritySelected = viewModel::setExposurePriority,
                    onIsoChanged = viewModel::setIso,
                    onShutterChanged = viewModel::setShutterNanos,
                    onCompensationChanged = viewModel::setExposureCompensation
                )
                PanoramaLivePreview(state)
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LiveStatusStrip(state)
                        RecordingHud(
                            state = state,
                            compact = compact,
                            onPauseClicked = viewModel::pauseOrResume,
                            onMuteClicked = {
                                viewModel.onMicControlClicked(onRequestMicrophonePermission)
                            }
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(1f)
                    .onGloballyPositioned { footerHeightPx = it.size.height },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CameraFooter(
                    state = state,
                    compact = compact,
                    onModeSelected = viewModel::setMode,
                    onFlipClicked = viewModel::toggleLens,
                    onShutterClicked = { viewModel.onShutter(previewView) },
                    onGalleryClicked = onGalleryClicked,
                    onEffectSelected = viewModel::setEffect,
                    onZoomSelected = viewModel::setZoom
                )
            }
            CountdownOverlay(state.countdownRemaining)
            state.review?.let { review ->
                AppTheme {
                    CaptureConfirmOverlay(
                        review = review,
                        onRetake = viewModel::retakeCapture,
                        onKeep = viewModel::keepCapture
                    )
                }
            }
            CameraAlertDialog(
                show = showActiveCaptureExitDialog,
                title = stringResource(R.string.active_capture_exit_title),
                text = stringResource(R.string.active_capture_exit_body),
                confirmLabel = stringResource(R.string.active_capture_save),
                dismissLabel = stringResource(R.string.active_capture_discard),
                onDismiss = { showActiveCaptureExitDialog = false },
                onDismissLabel = viewModel::discardActiveCapture,
                onConfirm = viewModel::saveActiveCapture
            )
        }
    }
}

@DarkLightPreviews
@Composable
private fun CameraChromePreview() {
    val previewState = CameraUiState(
        hasFlash = true,
        gridEnabled = true,
        mode = com.arindam.camerax.domain.model.CameraMode.PHOTO,
        zoomRatio = 1f,
        minZoom = 0.5f,
        maxZoom = 5f,
        isCameraReady = true
    )
    AppTheme {
        Box(Modifier.fillMaxSize()) {
            CameraHeader(
                state = previewState,
                onFlashClicked = {},
                onTimerClicked = {},
                onGridClicked = {},
                onMotionClicked = {},
                onSettingsClicked = {}
            )
            Box(Modifier.align(Alignment.BottomCenter)) {
                CameraFooter(
                    state = previewState,
                    onModeSelected = {},
                    onFlipClicked = {},
                    onShutterClicked = {},
                    onGalleryClicked = {},
                    onEffectSelected = {}
                )
            }
        }
    }
}

private const val OTHERS_ENTER_MILLIS = 360L
