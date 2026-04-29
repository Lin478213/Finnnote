package com.example.y.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.y.ui.component.glass.LiquidGlassColumnPanel
import com.example.y.ui.component.glass.LiquidGlassTopBar
import com.example.y.ui.theme.AppLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToBackground: () -> Unit = {},
    onNavigateToAccountManagement: () -> Unit = {},
    onNavigateToCategoryManagement: () -> Unit = {},
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LiquidGlassTopBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("设置") },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding),
            contentPadding = PaddingValues(
                horizontal = AppLayout.ScreenHorizontalPadding,
                vertical = AppLayout.ScreenItemSpacing,
            ),
            verticalArrangement = Arrangement.spacedBy(AppLayout.ScreenItemSpacing),
        ) {
            // ===== 通用设置 =====
            item {
                Text(
                    text = "通用",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        horizontal = AppLayout.SectionLabelHorizontalPadding,
                        vertical = AppLayout.SectionLabelVerticalPadding,
                    ),
                )
            }

            item {
                LiquidGlassColumnPanel {
                    ListItem(
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                        ),
                        headlineContent = { Text("自定义背景") },
                        supportingContent = { Text("设置背景图片和模糊效果") },
                        leadingContent = {
                            Icon(Icons.Default.Wallpaper, contentDescription = null)
                        },
                        trailingContent = {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                modifier = Modifier.padding(4.dp),
                            )
                        },
                        modifier = Modifier.clickable { onNavigateToBackground() },
                    )
                }
            }

            // ===== 数据管理 =====
            item {
                Text(
                    text = "数据管理",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        horizontal = AppLayout.SectionLabelHorizontalPadding,
                        vertical = AppLayout.SectionLabelVerticalPadding,
                    ),
                )
            }

            item {
                LiquidGlassColumnPanel {
                    ListItem(
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                        ),
                        headlineContent = { Text("分类管理") },
                        supportingContent = { Text("新增、编辑、归档与删除分类") },
                        leadingContent = {
                            Icon(Icons.Default.Category, contentDescription = null)
                        },
                        trailingContent = {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                modifier = Modifier.padding(4.dp),
                            )
                        },
                        modifier = Modifier.clickable { onNavigateToCategoryManagement() },
                    )
                    ListItem(
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                        ),
                        headlineContent = { Text("账户管理") },
                        supportingContent = { Text("添加、编辑、归档账户") },
                        leadingContent = {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
                        },
                        trailingContent = {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                modifier = Modifier.padding(4.dp),
                            )
                        },
                        modifier = Modifier.clickable { onNavigateToAccountManagement() },
                    )
                }
            }

            // ===== 关于 =====
            item {
                Text(
                    text = "关于",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        horizontal = AppLayout.SectionLabelHorizontalPadding,
                        vertical = AppLayout.SectionLabelVerticalPadding,
                    ),
                )
            }

            item {
                LiquidGlassColumnPanel {
                    ListItem(
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                        ),
                        headlineContent = { Text("版本") },
                        supportingContent = { Text("1.0.0") },
                        leadingContent = {
                            Icon(Icons.Default.Info, contentDescription = null)
                        },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(AppLayout.ScreenItemSpacing)) }
        }
    }
}
