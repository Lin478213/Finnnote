package com.example.y.ui.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Liquid Glass 风格的卡片
 * 提供玻璃态背景效果
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    alpha: Float = 0.4f,
    blurRadius: androidx.compose.ui.unit.Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val baseColor = Color(0xFFFAFAFA)

    Column(
        modifier = modifier
            .blur(blurRadius)
            .background(
                baseColor.copy(alpha = alpha),
                shape = RoundedCornerShape(cornerRadius),
            ),
        content = content,
    )
}

/**
 * Liquid Glass 风格的按钮卡片
 * 用于按钮、标签等交互元素
 */
@Composable
fun LiquidGlassButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    cornerRadius: androidx.compose.ui.unit.Dp = 20.dp,
    alpha: Float = 0.5f,
    content: @Composable () -> Unit,
) {
    val baseColor = Color(0xFFFAFAFA)

    Box(
        modifier = modifier
            .blur(2.dp)
            .background(
                baseColor.copy(alpha = alpha),
                shape = RoundedCornerShape(cornerRadius),
            )
            .then(
                if (onClick != {}) Modifier else Modifier,
            ),
    ) {
        content()
    }
}
