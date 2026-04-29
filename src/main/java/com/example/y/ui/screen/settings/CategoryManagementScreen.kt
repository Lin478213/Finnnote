package com.example.y.ui.screen.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.y.data.model.Category
import com.example.y.data.model.TransactionType
import com.example.y.ui.component.glass.LiquidGlassColumnPanel
import com.example.y.ui.component.glass.LiquidGlassPanel
import com.example.y.ui.component.glass.LiquidGlassSegmentedControl
import com.example.y.ui.component.glass.LiquidGlassTextFieldShape
import com.example.y.ui.component.glass.LiquidGlassTopBar
import com.example.y.ui.component.glass.liquidGlassOutlinedTextFieldColors
import com.example.y.ui.component.glass.liquidGlassPressEffect
import com.example.y.ui.theme.AppLayout
import com.example.y.ui.theme.LocalLiquidGlassBackdrop
import com.example.y.ui.viewmodel.CategoryManagementViewModel
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import kotlinx.coroutines.flow.collectLatest

private val categoryPresetColors = listOf(
    0xFFE91E63,
    0xFFF44336,
    0xFFFF5722,
    0xFFFF9800,
    0xFFFFC107,
    0xFF4CAF50,
    0xFF009688,
    0xFF00BCD4,
    0xFF2196F3,
    0xFF3F51B5,
    0xFF673AB7,
    0xFF9C27B0,
    0xFF795548,
    0xFF607D8B,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    onBack: () -> Unit,
    viewModel: CategoryManagementViewModel = hiltViewModel(),
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val backInteraction = remember { MutableInteractionSource() }
    val addInteraction = remember { MutableInteractionSource() }

    var selectedTypeIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedType = if (selectedTypeIndex == 0) TransactionType.EXPENSE else TransactionType.INCOME
    val typeCategories = remember(categories, selectedType) {
        categories
            .filter { it.type == selectedType }
            .sortedWith(compareBy<Category> { it.isArchived }.thenBy { it.sortOrder }.thenBy { it.id })
    }
    val activeCategories = remember(typeCategories) { typeCategories.filter { !it.isArchived } }
    val archivedCategories = remember(typeCategories) { typeCategories.filter { it.isArchived } }

    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var deletingCategory by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.messages.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            LiquidGlassTopBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("分类管理") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        interactionSource = backInteraction,
                        modifier = Modifier.liquidGlassPressEffect(backInteraction, pressedScale = 1.06f),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingCategory = null
                    showEditDialog = true
                },
                interactionSource = addInteraction,
                modifier = Modifier.liquidGlassPressEffect(addInteraction, pressedScale = 1.06f),
            ) {
                Icon(Icons.Default.Add, contentDescription = "新增分类")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AppLayout.ScreenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(AppLayout.ScreenSectionSpacing),
        ) {
            item {
                LiquidGlassSegmentedControl(
                    options = listOf("支出分类", "收入分类"),
                    selectedIndex = selectedTypeIndex,
                    onSelected = { selectedTypeIndex = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                SectionHeader("可用分类 (${activeCategories.size})")
            }

            item {
                if (activeCategories.isEmpty()) {
                    EmptyStateCard("暂无可用分类，点击右下角新增")
                } else {
                    CategoryListPanel(
                        categories = activeCategories,
                        onEdit = {
                            editingCategory = it
                            showEditDialog = true
                        },
                        onToggleArchive = { viewModel.setArchived(it.id, archived = true) },
                        onDelete = { deletingCategory = it },
                    )
                }
            }

            item {
                SectionHeader("已归档 (${archivedCategories.size})")
            }

            item {
                if (archivedCategories.isEmpty()) {
                    EmptyStateCard("无归档分类")
                } else {
                    CategoryListPanel(
                        categories = archivedCategories,
                        onEdit = {
                            editingCategory = it
                            showEditDialog = true
                        },
                        onToggleArchive = { viewModel.setArchived(it.id, archived = false) },
                        onDelete = { deletingCategory = it },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(AppLayout.BottomFabSpacer)) }
        }
    }

    if (showEditDialog) {
        CategoryEditDialog(
            category = editingCategory,
            defaultType = selectedType,
            onDismiss = { showEditDialog = false },
            onConfirm = { type, name, color ->
                val editing = editingCategory
                if (editing == null) {
                    viewModel.addCategory(
                        name = name,
                        color = color,
                        type = type,
                    )
                } else {
                    viewModel.updateCategory(
                        categoryId = editing.id,
                        name = name,
                        color = color,
                    )
                }
                showEditDialog = false
            },
        )
    }

    deletingCategory?.let { category ->
        val confirmInteraction = remember { MutableInteractionSource() }
        val cancelInteraction = remember { MutableInteractionSource() }
        AlertDialog(
            onDismissRequest = { deletingCategory = null },
            title = { Text("删除分类") },
            text = { Text("确认删除“${category.name}”？历史账单将显示为未分类。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCategory(category.id)
                        deletingCategory = null
                    },
                    interactionSource = confirmInteraction,
                    modifier = Modifier.liquidGlassPressEffect(confirmInteraction, pressedScale = 1.06f),
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deletingCategory = null },
                    interactionSource = cancelInteraction,
                    modifier = Modifier.liquidGlassPressEffect(cancelInteraction, pressedScale = 1.06f),
                ) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            horizontal = AppLayout.SectionLabelHorizontalPadding,
            vertical = AppLayout.SectionLabelVerticalPadding,
        ),
    )
}

@Composable
private fun EmptyStateCard(text: String) {
    LiquidGlassPanel(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(AppLayout.PanelContentPadding),
        )
    }
}

@Composable
private fun CategoryListPanel(
    categories: List<Category>,
    onEdit: (Category) -> Unit,
    onToggleArchive: (Category) -> Unit,
    onDelete: (Category) -> Unit,
) {
    LiquidGlassColumnPanel(modifier = Modifier.fillMaxWidth()) {
        categories.forEachIndexed { index, category ->
            CategoryRow(
                category = category,
                onEdit = { onEdit(category) },
                onToggleArchive = { onToggleArchive(category) },
                onDelete = { onDelete(category) },
            )
            if (index < categories.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = AppLayout.PanelContentPadding),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f),
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    onEdit: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val menuInteraction = remember { MutableInteractionSource() }
    val backdrop = LocalLiquidGlassBackdrop.current
    val menuShape = RoundedCornerShape(24.dp)
    val menuContainerColor = Color.White.copy(alpha = 0.92f)
    val menuBorderColor = Color.White.copy(alpha = 0.98f)
    val menuInnerBorderColor = Color.White.copy(alpha = 0.66f)
    val menuGradient = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.98f),
            Color.White.copy(alpha = 0.9f),
        ),
    )
    val menuGlassModifier = if (backdrop == null) {
        Modifier.background(menuContainerColor, menuShape)
    } else {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { menuShape },
            effects = {
                vibrancy()
                blur(16f.dp.toPx())
                lens(18f.dp.toPx(), 24f.dp.toPx())
            },
            highlight = { Highlight.Default.copy(alpha = 1f) },
            onDrawSurface = { drawRect(menuContainerColor) },
        )
    }
        .shadow(
            elevation = 8.dp,
            shape = menuShape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.24f),
            spotColor = Color.Black.copy(alpha = 0.18f),
        )
        .background(menuGradient, menuShape)
        .drawWithContent {
            drawContent()
            drawRoundRect(
                color = Color.White.copy(alpha = 0.22f),
                topLeft = Offset(0f, 0f),
                size = Size(size.width, size.height * 0.48f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(120f, 120f),
            )
        }
    val menuItemColors = MenuDefaults.itemColors(
        textColor = MaterialTheme.colorScheme.onSurface,
        leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(category.color)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = category.name.take(1),
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = category.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                )
                if (category.isDefault) {
                    Spacer(modifier = Modifier.size(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "默认",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        },
        supportingContent = {
            Text(
                text = buildString {
                    append(if (category.type == TransactionType.EXPENSE) "支出分类" else "收入分类")
                    if (category.isArchived) append(" · 已归档")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    interactionSource = menuInteraction,
                    modifier = Modifier.liquidGlassPressEffect(menuInteraction, pressedScale = 1.06f),
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更多")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = menuGlassModifier
                        .clip(menuShape)
                        .border(1.6.dp, menuBorderColor, menuShape)
                        .border(0.8.dp, menuInnerBorderColor, menuShape),
                    shape = menuShape,
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    border = BorderStroke(0.dp, Color.Transparent),
                ) {
                    DropdownMenuItem(
                        text = { Text("编辑") },
                        colors = menuItemColors,
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        },
                    )
                    if (!category.isDefault) {
                        DropdownMenuItem(
                            text = { Text(if (category.isArchived) "恢复分类" else "归档分类") },
                            colors = menuItemColors,
                            onClick = {
                                menuExpanded = false
                                onToggleArchive()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("删除分类") },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.error,
                                leadingIconColor = MaterialTheme.colorScheme.error,
                            ),
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun CategoryEditDialog(
    category: Category?,
    defaultType: TransactionType,
    onDismiss: () -> Unit,
    onConfirm: (type: TransactionType, name: String, color: Long) -> Unit,
) {
    var selectedTypeIndex by remember(category, defaultType) {
        mutableIntStateOf(
            when (category?.type ?: defaultType) {
                TransactionType.EXPENSE -> 0
                TransactionType.INCOME -> 1
            },
        )
    }
    var name by remember(category) { mutableStateOf(category?.name.orEmpty()) }
    var selectedColor by remember(category) { mutableStateOf(category?.color ?: categoryPresetColors.first()) }
    val scrollState = rememberScrollState()
    val confirmInteraction = remember { MutableInteractionSource() }
    val cancelInteraction = remember { MutableInteractionSource() }
    val canConfirm = name.trim().isNotBlank()
    val isEditing = category != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "编辑分类" else "新增分类") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!isEditing) {
                    LiquidGlassSegmentedControl(
                        options = listOf("支出", "收入"),
                        selectedIndex = selectedTypeIndex,
                        onSelected = { selectedTypeIndex = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(
                        text = if (category.type == TransactionType.EXPENSE) "支出分类" else "收入分类",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 12) name = it },
                    singleLine = true,
                    label = { Text("分类名称") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = LiquidGlassTextFieldShape,
                    colors = liquidGlassOutlinedTextFieldColors(),
                )
                Column {
                    Text(
                        text = "分类颜色",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        categoryPresetColors.forEach { color ->
                            val interaction = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(color))
                                    .border(
                                        width = if (selectedColor == color) 2.dp else 1.dp,
                                        color = if (selectedColor == color) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        },
                                        shape = CircleShape,
                                    )
                                    .liquidGlassPressEffect(
                                        interactionSource = interaction,
                                        pressedScale = 1.1f,
                                    )
                                    .clickable(
                                        interactionSource = interaction,
                                        indication = null,
                                        onClick = { selectedColor = color },
                                    ),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        if (selectedTypeIndex == 0) TransactionType.EXPENSE else TransactionType.INCOME,
                        name.trim(),
                        selectedColor,
                    )
                },
                enabled = canConfirm,
                interactionSource = confirmInteraction,
                modifier = Modifier.liquidGlassPressEffect(
                    interactionSource = confirmInteraction,
                    enabled = canConfirm,
                    pressedScale = 1.06f,
                ),
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                interactionSource = cancelInteraction,
                modifier = Modifier.liquidGlassPressEffect(cancelInteraction, pressedScale = 1.06f),
            ) {
                Text("取消")
            }
        },
    )
}
