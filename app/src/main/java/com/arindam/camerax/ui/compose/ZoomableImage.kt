package com.arindam.camerax.ui.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Presentation: photo viewer supporting pinch-to-zoom, pan, and double-tap zoom.
 *
 * When scale is 1x, 1-finger horizontal gestures are not consumed so parent [androidx.compose.foundation.pager.HorizontalPager]
 * can swipe naturally. When zoomed in (>1.05x), pan gestures move the photo and [onZoomChanged]
 * notifies the parent to lock pager scrolling.
 */
@Composable
fun ZoomableImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    maxScale: Float = 6.0f,
    onZoomChanged: (Boolean) -> Unit = {},
    onTap: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1f) }
    val offsetXAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(0f) }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    // Reset zoom and pan whenever the displayed image changes
    LaunchedEffect(model) {
        scaleAnim.snapTo(1f)
        offsetXAnim.snapTo(0f)
        offsetYAnim.snapTo(0f)
    }

    // Notify parent if the active photo is currently zoomed
    val isZoomed = scaleAnim.value > 1.05f
    LaunchedEffect(isZoomed) {
        onZoomChanged(isZoomed)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(model) {
                var lastTapTime = 0L
                var lastTapPos = Offset.Zero
                val touchSlop = viewConfiguration.touchSlop

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var dragDistance = 0f
                    var isPinching = false

                    do {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.filter { it.pressed }
                        if (pressed.isEmpty()) break

                        if (pressed.size >= 2) {
                            // Multi-touch: pinch-to-zoom and pan
                            isPinching = true
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()

                            val targetScale = (scaleAnim.value * zoom).coerceIn(0.75f, maxScale)
                            val maxOffsetX = (containerSize.width * (targetScale.coerceAtLeast(1f) - 1f)) / 2f
                            val maxOffsetY = (containerSize.height * (targetScale.coerceAtLeast(1f) - 1f)) / 2f

                            val targetX = (offsetXAnim.value + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                            val targetY = (offsetYAnim.value + pan.y).coerceIn(-maxOffsetY, maxOffsetY)

                            coroutineScope.launch {
                                scaleAnim.snapTo(targetScale)
                                offsetXAnim.snapTo(targetX)
                                offsetYAnim.snapTo(targetY)
                            }
                            pressed.forEach { it.consume() }
                        } else if (pressed.size == 1 && !isPinching) {
                            val pointer = pressed.first()
                            val pan = pointer.positionChange()
                            dragDistance += pan.getDistance()

                            if (scaleAnim.value > 1.05f) {
                                // Zoomed in: 1-finger pan
                                val maxOffsetX = (containerSize.width * (scaleAnim.value - 1f)) / 2f
                                val maxOffsetY = (containerSize.height * (scaleAnim.value - 1f)) / 2f

                                val targetX = (offsetXAnim.value + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                                val targetY = (offsetYAnim.value + pan.y).coerceIn(-maxOffsetY, maxOffsetY)

                                coroutineScope.launch {
                                    offsetXAnim.snapTo(targetX)
                                    offsetYAnim.snapTo(targetY)
                                }
                                if (pointer.positionChanged()) {
                                    pointer.consume()
                                }
                            }
                            // When scaleAnim.value <= 1.05f, we do NOT consume pointer changes.
                            // This allows the parent HorizontalPager to swipe between photos.
                        }
                    } while (event.changes.any { it.pressed })

                    // Check if gesture was a tap
                    if (!isPinching && dragDistance < touchSlop) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastTapTime < 300L && (down.position - lastTapPos).getDistance() < touchSlop * 3) {
                            // Double-tap: toggle zoom
                            lastTapTime = 0L
                            coroutineScope.launch {
                                if (scaleAnim.value > 1.05f) {
                                    launch { scaleAnim.animateTo(1f, spring()) }
                                    launch { offsetXAnim.animateTo(0f, spring()) }
                                    launch { offsetYAnim.animateTo(0f, spring()) }
                                } else {
                                    val targetScale = 2.5f.coerceAtMost(maxScale)
                                    val center = Offset(containerSize.width / 2f, containerSize.height / 2f)
                                    val maxOffsetX = (containerSize.width * (targetScale - 1f)) / 2f
                                    val maxOffsetY = (containerSize.height * (targetScale - 1f)) / 2f
                                    val targetX = ((center.x - down.position.x) * (targetScale - 1f)).coerceIn(-maxOffsetX, maxOffsetX)
                                    val targetY = ((center.y - down.position.y) * (targetScale - 1f)).coerceIn(-maxOffsetY, maxOffsetY)

                                    launch { scaleAnim.animateTo(targetScale, spring()) }
                                    launch { offsetXAnim.animateTo(targetX, spring()) }
                                    launch { offsetYAnim.animateTo(targetY, spring()) }
                                }
                            }
                        } else {
                            lastTapTime = currentTime
                            lastTapPos = down.position
                            if (onTap != null) {
                                coroutineScope.launch {
                                    delay(300L)
                                    if (lastTapTime == currentTime) {
                                        onTap()
                                    }
                                }
                            }
                        }
                    }

                    // Snap back if released below 1.0f
                    if (scaleAnim.value < 1f) {
                        coroutineScope.launch {
                            launch { scaleAnim.animateTo(1f, spring()) }
                            launch { offsetXAnim.animateTo(0f, spring()) }
                            launch { offsetYAnim.animateTo(0f, spring()) }
                        }
                    } else if (scaleAnim.value > 1.05f) {
                        val maxOffsetX = (containerSize.width * (scaleAnim.value - 1f)) / 2f
                        val maxOffsetY = (containerSize.height * (scaleAnim.value - 1f)) / 2f
                        val targetX = offsetXAnim.value.coerceIn(-maxOffsetX, maxOffsetX)
                        val targetY = offsetYAnim.value.coerceIn(-maxOffsetY, maxOffsetY)
                        if (targetX != offsetXAnim.value || targetY != offsetYAnim.value) {
                            coroutineScope.launch {
                                launch { offsetXAnim.animateTo(targetX, spring()) }
                                launch { offsetYAnim.animateTo(targetY, spring()) }
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = model),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    translationX = offsetXAnim.value
                    translationY = offsetYAnim.value
                }
        )
    }
}
