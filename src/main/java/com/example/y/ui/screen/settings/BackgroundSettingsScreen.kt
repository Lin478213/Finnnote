package com.example.y.ui.screen.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.y.data.repository.BackgroundSettings
import com.example.y.ui.component.glass.LiquidGlassPanel
import com.example.y.ui.component.glass.LiquidGlassTopBar
import com.example.y.ui.component.glass.liquidGlassPressEffect
import com.example.y.ui.theme.AppLayout
import com.example.y.ui.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.backgroundSettings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val backButtonInteraction = remember { MutableInteractionSource() }

    // 图片选择器 —— 获取持久化 URI 权限
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        uri?.let {
            // 持久化读取权限，App 重启后仍可访问
            context.contentResolver.takePersistableUriPermission(
                it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
            viewModel.setBackgroundImage(it)
            if (!settings.enabled) viewModel.setBackgroundEnabled(true)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LiquidGlassTopBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("自定义背景") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        interactionSource = backButtonInteraction,
                        modifier = Modifier.liquidGlassPressEffect(
                            interactionSource = backButtonInteraction,
                            pressedScale = 1.06f,
                        ),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = AppLayout.ScreenHorizontalPadding,
                    vertical = AppLayout.ScreenItemSpacing,
                ),
            verticalArrangement = Arrangement.spacedBy(AppLayout.ScreenSectionSpacing),
        ) {
            // ===== 开关 =====
            EnableToggleCard(
                enabled = settings.enabled,
                onToggle = { viewModel.setBackgroundEnabled(it) },
            )

            // ===== 图片预览 + 选择 =====
            ImagePickerCard(
                settings = settings,
                onPickImage = { imagePicker.launch(arrayOf("image/*")) },
                onClearImage = {
                    viewModel.setBackgroundImage(null)
                    viewModel.setBackgroundEnabled(false)
                },
            )

            // ===== 模糊 & 暗度调节（仅在启用且有图片时展示） =====
            AnimatedVisibility(
                visible = settings.enabled && settings.imageUri.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BlurSliderCard(
                        blurRadius = settings.blurRadius,
                        onBlurChanged = { viewModel.setBlurRadius(it) },
                    )
                    DimSliderCard(
                        dimAlpha = settings.dimAlpha,
                        onDimChanged = { viewModel.setDimAlpha(it) },
                    )
                }
            }
        }
    }
}

// -------------------- 子组件 --------------------

@Composable
private fun EnableToggleCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    LiquidGlassPanel {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppLayout.PanelContentPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("启用自定义背景", style = MaterialTheme.typography.titleMedium)
                Text(
                    "选择一张图片作为全局背景",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun ImagePickerCard(
    settings: BackgroundSettings,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit,
) {
    val pickCardInteraction = remember { MutableInteractionSource() }
    val clearCardInteraction = remember { MutableInteractionSource() }
    val emptyCardInteraction = remember { MutableInteractionSource() }
    LiquidGlassPanel {
        Column(modifier = Modifier.padding(AppLayout.PanelContentPadding)) {
            Text("背景图片", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            if (settings.imageUri.isNotEmpty()) {
                // 预览（带实时模糊效果）
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(Uri.parse(settings.imageUri))
                            .crossfade(true)
                            .build(),
                        contentDescription = "背景预览",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (settings.blurRadius > 0f)
                                    Modifier.blur(settings.blurRadius.dp, BlurredEdgeTreatment.Rectangle)
                                else Modifier,
                            ),
                    )
                    // 暗度遮罩
                    if (settings.dimAlpha > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = settings.dimAlpha)),
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .liquidGlassPressEffect(
                                interactionSource = pickCardInteraction,
                                pressedScale = 1.03f,
                            )
                            .clickable(
                                interactionSource = pickCardInteraction,
                                indication = null,
                                onClick = onPickImage,
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Text("  更换", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .liquidGlassPressEffect(
                                interactionSource = clearCardInteraction,
                                pressedScale = 1.03f,
                            )
                            .clickable(
                                interactionSource = clearCardInteraction,
                                indication = null,
                                onClick = onClearImage,
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Text("  移除", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            } else {
                // 空状态 —— 点击选择
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(12.dp),
                        )
                        .liquidGlassPressEffect(
                            interactionSource = emptyCardInteraction,
                            pressedScale = 1.02f,
                        )
                        .clickable(
                            interactionSource = emptyCardInteraction,
                            indication = null,
                            onClick = onPickImage,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "点击选择图片",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BlurSliderCard(blurRadius: Float, onBlurChanged: (Float) -> Unit) {
    LiquidGlassPanel {
        Column(modifier = Modifier.padding(AppLayout.PanelContentPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.BlurOn,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "  模糊程度",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${blurRadius.roundToInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(8.dp))
            Slider(
                value = blurRadius,
                onValueChange = onBlurChanged,
                valueRange = 0f..25f,
                steps = 24,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("清晰", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("强模糊", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DimSliderCard(dimAlpha: Float, onDimChanged: (Float) -> Unit) {
    LiquidGlassPanel {
        Column(modifier = Modifier.padding(AppLayout.PanelContentPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Opacity,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "  背景暗度",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${(dimAlpha * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(8.dp))
            Slider(
                value = dimAlpha,
                onValueChange = onDimChanged,
                valueRange = 0f..0.8f,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("透明", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("较暗", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
