package com.example.y.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Liquid Glass 风格修饰符
 * 提供磨砂玻璃效果：高斯模糊 + 半透明背景 + 边框
 */
@Composable
fun Modifier.liquidGlass(
    blurRadius: Float = 10f,
    borderWidth: Float = 1f,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
): Modifier = this
    .blur(blurRadius.dp, BlurredEdgeTreatment.Unbounded)
    .background(
        color = containerColor,
        shape = shape,
    )
    .border(
        width = borderWidth.dp,
        color = borderColor,
        shape = shape,
    )

/**
 * Liquid Glass 导航栏背景
 */
@Composable
fun Modifier.liquidGlassBar(
    blurRadius: Float = 12f,
): Modifier = this
    .blur(blurRadius.dp, BlurredEdgeTreatment.Rectangle)
    .background(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
    )
    .border(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    )

/**
 * Liquid Glass 卡片修饰符
 */
@Composable
fun Modifier.liquidGlassCard(
    blurRadius: Float = 8f,
    borderWidth: Float = 1.5f,
    containerAlpha: Float = 0.65f,
): Modifier = this
    .blur(blurRadius.dp, BlurredEdgeTreatment.Unbounded)
    .background(
        color = MaterialTheme.colorScheme.surface.copy(alpha = containerAlpha),
        shape = RoundedCornerShape(20.dp),
    )
    .border(
        width = borderWidth.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        shape = RoundedCornerShape(20.dp),
    )
