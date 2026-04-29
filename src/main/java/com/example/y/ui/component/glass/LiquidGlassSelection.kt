package com.example.y.ui.component.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.y.ui.theme.LocalLiquidGlassBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight

@Composable
fun LiquidGlassSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (options.isEmpty()) return

    val backdrop = LocalLiquidGlassBackdrop.current
    val shape = RoundedCornerShape(18.dp)
    val indicatorShape = RoundedCornerShape(14.dp)

    val containerColor = Color.White.copy(alpha = 0.1f)
    val borderColor = Color.White.copy(alpha = 0.45f)
    val indicatorColor = Color.White.copy(alpha = 0.2f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(containerColor)
            .border(1.dp, borderColor, shape),
    ) {
        val safeIndex = selectedIndex.coerceIn(0, options.lastIndex)
        val itemWidth = maxWidth / options.size
        val indicatorOffset by animateDpAsState(
            targetValue = itemWidth * safeIndex,
            animationSpec = spring(stiffness = 560f, dampingRatio = 0.78f),
            label = "liquidSegmentIndicatorOffset",
        )

        Box(
            modifier = Modifier
                .padding(4.dp)
                .offset(x = indicatorOffset)
                .width(itemWidth - 8.dp)
                .fillMaxHeight()
                .then(
                    if (backdrop == null) {
                        Modifier.background(indicatorColor, indicatorShape)
                    } else {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { indicatorShape },
                            effects = {
                                vibrancy()
                                blur(6f.dp.toPx())
                                lens(8f.dp.toPx(), 12f.dp.toPx())
                            },
                            highlight = { Highlight.Default.copy(alpha = 0.9f) },
                            onDrawSurface = { drawRect(indicatorColor) },
                        )
                    },
                )
                .border(1.dp, borderColor.copy(alpha = 0.9f), indicatorShape),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            options.forEachIndexed { index, option ->
                LiquidSegmentItem(
                    text = option,
                    selected = safeIndex == index,
                    onClick = { onSelected(index) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
fun LiquidGlassChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backdrop = LocalLiquidGlassBackdrop.current
    val shape = RoundedCornerShape(999.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val horizontalPadding by animateDpAsState(
        targetValue = when {
            pressed -> 19.dp
            selected -> 17.dp
            else -> 14.dp
        },
        animationSpec = spring(stiffness = 520f, dampingRatio = 0.86f),
        label = "liquidChipHorizontalPadding",
    )
    val verticalPadding by animateDpAsState(
        targetValue = when {
            pressed -> 10.dp
            selected -> 9.dp
            else -> 8.dp
        },
        animationSpec = spring(stiffness = 520f, dampingRatio = 0.86f),
        label = "liquidChipVerticalPadding",
    )
    val borderWidth by animateDpAsState(
        targetValue = if (selected) 1.2.dp else 1.dp,
        animationSpec = spring(stiffness = 520f, dampingRatio = 0.86f),
        label = "liquidChipBorderWidth",
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "liquidChipTextColor",
    )

    val baseColor =
        if (selected) {
            Color.White.copy(alpha = 0.22f)
        } else {
            Color.White.copy(alpha = 0.1f)
        }
    val borderColor =
        if (selected) {
            Color.White.copy(alpha = 0.56f)
        } else {
            Color.White.copy(alpha = 0.4f)
        }

    Box(
        modifier = modifier
            .liquidGlassPressEffect(
                interactionSource = interactionSource,
                pressedScale = if (selected) 1.12f else 1.1f,
                restingScale = if (selected) 1.04f else 1f,
            )
            .then(
                if (selected && backdrop != null) {
                    Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { shape },
                        effects = {
                            vibrancy()
                            blur(4f.dp.toPx())
                            lens(6f.dp.toPx(), 8f.dp.toPx())
                        },
                        highlight = { Highlight.Ambient.copy(alpha = 0.9f) },
                        onDrawSurface = { drawRect(baseColor) },
                    )
                } else {
                    Modifier.background(baseColor, shape)
                },
            )
            .border(borderWidth, borderColor, shape)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor,
        )
    }
}

@Composable
private fun LiquidSegmentItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "liquidSegmentTextColor",
    )
    val itemHorizontalPadding by animateDpAsState(
        targetValue = when {
            pressed -> 6.dp
            selected -> 4.dp
            else -> 2.dp
        },
        animationSpec = spring(stiffness = 520f, dampingRatio = 0.86f),
        label = "liquidSegmentHorizontalPadding",
    )

    Box(
        modifier = modifier
            .liquidGlassPressEffect(
                interactionSource = interactionSource,
                pressedScale = if (selected) 1.1f else 1.08f,
                restingScale = if (selected) 1.03f else 1f,
            )
            .padding(horizontal = itemHorizontalPadding)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor,
        )
    }
}
