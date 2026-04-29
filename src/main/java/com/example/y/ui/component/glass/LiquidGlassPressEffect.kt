package com.example.y.ui.component.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun Modifier.liquidGlassPressEffect(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
    pressedScale: Float = 1.08f,
    restingScale: Float = 1f,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && pressed) pressedScale else restingScale,
        animationSpec = spring(stiffness = 460f, dampingRatio = 0.84f),
        label = "liquidGlassPressScale",
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
