package com.example.y.ui.component.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.y.ui.theme.LocalLiquidGlassBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidGlassTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {},
    windowInsets: WindowInsets = WindowInsets(0, 0, 0, 0),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
    ),
) {
    val backdrop = LocalLiquidGlassBackdrop.current
    // Use a CornerBasedShape so lens effects are supported
    val shape = RoundedCornerShape(0.dp)
    val surfaceColor = Color.White.copy(alpha = 0.22f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (backdrop == null) {
                    Modifier
                } else {
                    Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { shape },
                        effects = {
                            vibrancy()
                            blur(6f.dp.toPx())
                            lens(10f.dp.toPx(), 12f.dp.toPx())
                        },
                        highlight = { Highlight.Ambient.copy(alpha = 0.8f) },
                        onDrawSurface = { drawRect(surfaceColor) },
                    )
                }
            ),
    ) {
        TopAppBar(
            title = title,
            windowInsets = windowInsets,
            navigationIcon = navigationIcon,
            actions = { actions() },
            scrollBehavior = scrollBehavior,
            colors = colors,
        )
    }
}
