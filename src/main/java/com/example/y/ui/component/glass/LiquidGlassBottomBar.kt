package com.example.y.ui.component.glass

import android.view.MotionEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.core.snap
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.example.y.ui.navigation.TopDestination
import com.example.y.ui.screen.common.LiquidGlassNavItem
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow

@Composable
fun LiquidGlassBottomBar(
    destinations: List<TopDestination>,
    currentRoute: String?,
    backdrop: Backdrop,
    onDestinationSelected: (TopDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (destinations.isEmpty()) return

    val routeSelectedIndex = destinations.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    val density = LocalDensity.current

    val containerColor = Color(0xFFFAFAFA).copy(alpha = 0.4f)
    val activeColor = Color.White.copy(alpha = 0.18f)
    val borderColor = Color.White.copy(alpha = 0.58f)
    val shape = RoundedCornerShape(32.dp)
    val selectedShape = RoundedCornerShape(50)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        val tabWidth = maxWidth / destinations.size
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val tabWidthPx = maxWidthPx / destinations.size
        val minCenter = tabWidthPx / 2f
        val maxCenter = maxWidthPx - tabWidthPx / 2f

        var isDragging by remember { mutableStateOf(false) }
        var selectedIndex by remember { mutableIntStateOf(routeSelectedIndex) }
        var dragCenterX by remember { mutableFloatStateOf((selectedIndex + 0.5f) * tabWidthPx) }

        LaunchedEffect(routeSelectedIndex) {
            selectedIndex = routeSelectedIndex
        }

        LaunchedEffect(selectedIndex, tabWidthPx, isDragging) {
            if (!isDragging) {
                dragCenterX = (selectedIndex + 0.5f) * tabWidthPx
            }
        }

        val settledCenterX by animateFloatAsState(
            targetValue = if (isDragging) dragCenterX.coerceIn(minCenter, maxCenter) else (selectedIndex + 0.5f) * tabWidthPx,
            animationSpec = if (isDragging) snap() else spring(stiffness = 500f, dampingRatio = 0.94f),
            label = "bottomBarSettledCenterX",
        )
        val pressTransition = updateTransition(
            targetState = isDragging,
            label = "bottomBarPressTransition",
        )
        val pressProgress by pressTransition.animateFloat(
            transitionSpec = {
                if (targetState) spring(stiffness = 560f, dampingRatio = 0.9f)
                else spring(stiffness = 420f, dampingRatio = 0.82f)
            },
            label = "bottomBarPressProgress",
        ) { pressed -> if (pressed) 1f else 0f }
        val pressScaleX by pressTransition.animateFloat(
            transitionSpec = {
                if (targetState) spring(stiffness = 520f, dampingRatio = 0.9f)
                else spring(stiffness = 360f, dampingRatio = 0.86f)
            },
            label = "bottomBarPressScaleX",
        ) { pressed -> if (pressed) 1.03f else 1f }
        val pressScaleY by pressTransition.animateFloat(
            transitionSpec = {
                if (targetState) spring(stiffness = 520f, dampingRatio = 0.9f)
                else spring(stiffness = 360f, dampingRatio = 0.86f)
            },
            label = "bottomBarPressScaleY",
        ) { pressed -> if (pressed) 1.03f else 1f }

        val indicatorCenterX = settledCenterX
        val indicatorWidth = lerp(tabWidth + 18.dp, tabWidth + 28.dp, pressProgress)
        val indicatorVerticalPaddingRaw = lerp((-2).dp, (-8).dp, pressProgress)
        // Ensure padding is never negative to avoid Compose IllegalArgumentException
        val indicatorVerticalPadding = indicatorVerticalPaddingRaw.coerceAtLeast(0.dp)
        val indicatorEnvelopeScale = 1.08f + (0.08f * pressProgress)

        val indicatorWidthPx = with(density) { indicatorWidth.toPx() }
        val maxOffsetPx = (maxWidthPx - indicatorWidthPx).coerceAtLeast(0f)
        val indicatorOffsetPx = (indicatorCenterX - indicatorWidthPx / 2f)
            .coerceIn(0f, maxOffsetPx)
        val indicatorOffset = with(density) { indicatorOffsetPx.toDp() }
        val activeIndex = ((indicatorCenterX / tabWidthPx).toInt()).coerceIn(0, destinations.lastIndex)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .pointerInteropFilter { event ->
                    val clamped = event.x.coerceIn(minCenter, maxCenter)
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            isDragging = true
                            dragCenterX = clamped
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (isDragging) {
                                dragCenterX = clamped
                            }
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            if (isDragging) {
                                dragCenterX = clamped
                                val targetIndex = ((dragCenterX / tabWidthPx).toInt())
                                    .coerceIn(0, destinations.lastIndex)
                                selectedIndex = targetIndex
                                onDestinationSelected(destinations[targetIndex])
                            }
                            isDragging = false
                            true
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            isDragging = false
                            true
                        }
                        else -> false
                    }
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { shape },
                        effects = {
                            vibrancy()
                            blur(8f.dp.toPx())
                            lens(20f.dp.toPx(), 16f.dp.toPx())
                        },
                        highlight = { Highlight.Ambient.copy(alpha = 0.9f) },
                        shadow = {
                            Shadow(
                                radius = 8.dp,
                                color = Color.Black.copy(alpha = 0.22f),
                            )
                        },
                        onDrawSurface = { drawRect(containerColor) },
                    )
                    .border(1.dp, borderColor, shape),
            )

            Box(
                modifier = Modifier
                    .padding(vertical = indicatorVerticalPadding.coerceAtLeast(0.dp))
                    .offset(x = indicatorOffset)
                    .width(indicatorWidth)
                    .fillMaxHeight()
                    .padding(horizontal = 3.dp)
                    .graphicsLayer {
                        scaleX = pressScaleX * indicatorEnvelopeScale
                        scaleY = pressScaleY * indicatorEnvelopeScale
                    }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { selectedShape },
                        effects = {
                            blur(8f.dp.toPx())
                            lens(
                                10f.dp.toPx(),
                                14f.dp.toPx(),
                                chromaticAberration = true,
                            )
                        },
                        highlight = { Highlight.Default.copy(alpha = 0.92f) },
                        shadow = { Shadow(alpha = 0.55f) },
                        innerShadow = {
                            InnerShadow(
                                radius = 5.dp,
                                alpha = 0.35f,
                            )
                        },
                        onDrawSurface = { drawRect(activeColor) },
                    ),
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                destinations.forEach { destination ->
                    val selected = destination.route == destinations[activeIndex].route
                    LiquidGlassNavItem(
                        selected = selected,
                        onClick = { onDestinationSelected(destination) },
                        icon = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        label = destination.label,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}
