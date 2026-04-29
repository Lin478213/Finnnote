package com.example.y.ui.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.y.ui.theme.LocalLiquidGlassBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow

@Composable
fun LiquidGlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val glassModifier = liquidGlassPanelModifier()

    Box(
        modifier = modifier
            .then(glassModifier),
        content = content,
    )
}

@Composable
fun LiquidGlassColumnPanel(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    val glassModifier = liquidGlassPanelModifier()

    Column(
        modifier = modifier
            .then(glassModifier),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

@Composable
private fun liquidGlassPanelModifier(): Modifier {
    val backdrop = LocalLiquidGlassBackdrop.current
    val shape = RoundedCornerShape(24.dp)
    val surfaceColor = Color.White.copy(alpha = 0.36f)
    val borderColor = Color.White.copy(alpha = 0.56f)
    val shadowColor = Color.Black.copy(alpha = 0.08f)

    val glassModifier = if (backdrop == null) {
        Modifier.background(surfaceColor, shape)
    } else {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur(8f.dp.toPx())
                lens(14f.dp.toPx(), 20f.dp.toPx())
            },
            highlight = { Highlight.Ambient.copy(alpha = 0.85f) },
            shadow = {
                Shadow(
                    radius = 8.dp,
                    color = shadowColor,
                )
            },
            innerShadow = {
                InnerShadow(
                    radius = 3.dp,
                    alpha = 0.25f,
                )
            },
            onDrawSurface = { drawRect(surfaceColor) },
        )
    }

    return glassModifier
        .clip(shape)
        .border(1.dp, borderColor, shape)
}
