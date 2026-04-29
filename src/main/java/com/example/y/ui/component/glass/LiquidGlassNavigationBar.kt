package com.example.y.ui.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Liquid Glass 风格的导航栏
 * 基于 Backdrop 库的玻璃态效果
 */
@Composable
fun LiquidGlassNavigationBar(
    modifier: Modifier = Modifier,
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .blur(8.dp)
            .background(
                Color(0xFFFAFAFA).copy(alpha = 0.5f),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                LiquidGlassNavigationItem(
                    item = item,
                    selected = isSelected,
                    onClick = { onItemSelected(index) },
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                )
            }
        }
    }
}

@Composable
private fun LiquidGlassNavigationItem(
    item: NavigationItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .liquidGlassPressEffect(
                interactionSource = interactionSource,
                pressedScale = 1.08f,
                restingScale = if (selected) 1.02f else 1f,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .background(
                if (selected)
                    primaryColor.copy(alpha = 0.3f)
                else
                    Color.Transparent,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp),
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = if (selected) primaryColor else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(bottom = 4.dp),
            )
            if (selected) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor,
                )
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)
