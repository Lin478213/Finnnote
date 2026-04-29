package com.example.y.ui.screen.bills

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.y.data.model.Category
import com.example.y.data.model.TransactionType
import com.example.y.data.model.TransactionWithDetails
import com.example.y.ui.component.glass.LiquidGlassChoiceChip
import com.example.y.ui.component.glass.LiquidGlassPanel
import com.example.y.ui.component.glass.LiquidGlassSegmentedControl
import com.example.y.ui.component.glass.LiquidGlassTopBar
import com.example.y.ui.component.glass.liquidGlassPressEffect
import com.example.y.ui.screen.add.AddTransactionSheet
import com.example.y.ui.screen.common.IncomeSummaryCard
import com.example.y.ui.theme.LocalLiquidGlassBackdrop
import com.example.y.ui.theme.AppLayout
import com.example.y.ui.theme.ExpenseRed
import com.example.y.ui.theme.IncomeGreen
import com.example.y.ui.util.formatAmount
import com.example.y.ui.util.formatDate
import com.example.y.ui.util.formatDayOfWeek
import com.example.y.ui.util.formatYearMonth
import com.example.y.ui.util.isToday
import com.example.y.ui.viewmodel.TransactionViewModel
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    onAddClick: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel(),
) {
    val dateRange by viewModel.dateRange.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val summary by viewModel.rangeSummary.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val prevMonthInteraction = remember { MutableInteractionSource() }
    val nextMonthInteraction = remember { MutableInteractionSource() }
    val fabInteraction = remember { MutableInteractionSource() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var editingTransaction by remember { mutableStateOf<TransactionWithDetails?>(null) }
    var deletingTransaction by remember { mutableStateOf<TransactionWithDetails?>(null) }
    var filterExpanded by rememberSaveable { mutableStateOf(false) }
    val currentMonthStart = currentMonthStartFromDevice()
    val canGoNextMonth = dateRange.first < currentMonthStart
    val selectedCategoryName = remember(selectedCategoryId, expenseCategories, incomeCategories) {
        if (selectedCategoryId == null) {
            "全部分类"
        } else {
            (expenseCategories + incomeCategories)
                .firstOrNull { it.id == selectedCategoryId }
                ?.name ?: "全部分类"
        }
    }

    LaunchedEffect(dateRange.first, currentMonthStart) {
        if (dateRange.first > currentMonthStart) {
            viewModel.setDateRange(
                startDate = currentMonthStart,
                endDate = nextMonthStart(currentMonthStart),
            )
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LiquidGlassTopBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = dateRange.first }
                                cal.add(Calendar.MONTH, -1)
                                val start = cal.timeInMillis
                                cal.add(Calendar.MONTH, 1)
                                viewModel.setDateRange(start, cal.timeInMillis)
                            },
                            interactionSource = prevMonthInteraction,
                            modifier = Modifier.liquidGlassPressEffect(
                                interactionSource = prevMonthInteraction,
                                pressedScale = 1.06f,
                            ),
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
                        }
                        Text(
                            text = dateRange.first.formatYearMonth(),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        IconButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = dateRange.first }
                                cal.add(Calendar.MONTH, 1)
                                val start = cal.timeInMillis
                                cal.add(Calendar.MONTH, 1)
                                viewModel.setDateRange(start, cal.timeInMillis)
                            },
                            enabled = canGoNextMonth,
                            interactionSource = nextMonthInteraction,
                            modifier = Modifier.liquidGlassPressEffect(
                                interactionSource = nextMonthInteraction,
                                enabled = canGoNextMonth,
                                pressedScale = 1.06f,
                            ),
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                modifier = Modifier.liquidGlassPressEffect(
                    interactionSource = fabInteraction,
                    pressedScale = 1.06f,
                ),
                interactionSource = fabInteraction,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(Icons.Default.Add, contentDescription = "记一笔")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(top = AppLayout.ScreenItemSpacing),
        ) {
            // 月度汇总卡片
            item {
                IncomeSummaryCard(
                    income = summary.totalIncome,
                    expense = summary.totalExpense,
                    modifier = Modifier.padding(
                        horizontal = AppLayout.ScreenHorizontalPadding,
                        vertical = AppLayout.ScreenItemSpacing,
                    ),
                )
            }

            // 分类筛选栏
            item {
                LiquidGlassPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = AppLayout.ScreenHorizontalPadding,
                            vertical = AppLayout.ScreenItemSpacing / 2,
                        ),
                ) {
                    val filterToggleInteraction = remember { MutableInteractionSource() }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .liquidGlassPressEffect(
                                    interactionSource = filterToggleInteraction,
                                    pressedScale = 1.02f,
                                )
                                .clickable(
                                    interactionSource = filterToggleInteraction,
                                    indication = null,
                                    onClick = { filterExpanded = !filterExpanded },
                                )
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "分类筛选",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = selectedCategoryName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (filterExpanded) "收起筛选" else "展开筛选",
                                modifier = Modifier.graphicsLayer {
                                    rotationZ = if (filterExpanded) 180f else 0f
                                },
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        AnimatedVisibility(
                            visible = filterExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            CategoryFilterRow(
                                expenseCategories = expenseCategories,
                                incomeCategories = incomeCategories,
                                selectedCategoryId = selectedCategoryId,
                                onCategorySelected = { viewModel.setCategoryFilter(it) },
                                showHeader = false,
                            )
                        }
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    EmptyBillsView(
                        modifier = Modifier
                            .fillParentMaxHeight(0.5f)
                            .fillMaxWidth(),
                    )
                }
            } else {
                // 按日期分组
                val grouped = transactions.groupBy { it.transaction.date.formatDate() }
                grouped.forEach { (dateLabel, items) ->
                    item {
                        DayHeader(
                            dateLabel = dateLabel,
                            dayOfWeek = items.first().transaction.date.formatDayOfWeek(),
                            isToday = items.first().transaction.date.isToday(),
                            dayTotal = items.sumOf {
                                if (it.transaction.type == TransactionType.EXPENSE) -it.transaction.amount
                                else it.transaction.amount
                            },
                        )
                    }
                    items(items, key = { it.transaction.id }) { detail ->
                        TransactionItem(
                            detail = detail,
                            onEdit = { editingTransaction = detail },
                            onDelete = { deletingTransaction = detail },
                        )
                    }
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = AppLayout.ScreenHorizontalPadding),
                        )
                    }
                }
            }

            // 底部占位，避免 FAB 遮挡
            item { Spacer(modifier = Modifier.height(AppLayout.BottomFabSpacer)) }
        }
    }

    if (editingTransaction != null) {
        AddTransactionSheet(
            onDismiss = { editingTransaction = null },
            editingTransaction = editingTransaction,
        )
    }

    deletingTransaction?.let { detail ->
        val confirmInteraction = remember { MutableInteractionSource() }
        val dismissInteraction = remember { MutableInteractionSource() }
        AlertDialog(
            onDismissRequest = { deletingTransaction = null },
            title = { Text("删除账单") },
            text = { Text("确认删除这条账单记录？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(detail.transaction.id)
                        deletingTransaction = null
                    },
                    interactionSource = confirmInteraction,
                    modifier = Modifier.liquidGlassPressEffect(confirmInteraction, pressedScale = 1.06f),
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deletingTransaction = null },
                    interactionSource = dismissInteraction,
                    modifier = Modifier.liquidGlassPressEffect(dismissInteraction, pressedScale = 1.06f),
                ) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun DayHeader(
    dateLabel: String,
    dayOfWeek: String,
    isToday: Boolean,
    dayTotal: Long,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = AppLayout.ScreenHorizontalPadding,
                vertical = AppLayout.SectionLabelVerticalPadding,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isToday) "今天" else dayOfWeek,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = dayTotal.formatAmount(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TransactionItem(
    detail: TransactionWithDetails,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val isExpense = detail.transaction.type == TransactionType.EXPENSE
    val amountColor = if (isExpense) ExpenseRed else IncomeGreen
    val sign = if (isExpense) "-" else "+"
    var menuExpanded by remember { mutableStateOf(false) }
    val moreActionInteraction = remember { MutableInteractionSource() }

    ListItem(
        headlineContent = {
            Text(
                text = detail.category?.name ?: "未分类",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = if (detail.transaction.note.isNotBlank()) {
            {
                Text(
                    text = detail.transaction.note,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else null,
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = (detail.category?.name ?: "?").take(1),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$sign${detail.transaction.amount.formatAmount()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = amountColor,
                )
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        interactionSource = moreActionInteraction,
                        modifier = Modifier.liquidGlassPressEffect(moreActionInteraction, pressedScale = 1.06f),
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("编辑") },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("删除") },
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
private fun EmptyBillsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outlineVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "还没有记录",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "点击 + 开始记账",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterRow(
    expenseCategories: List<Category>,
    incomeCategories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    showHeader: Boolean = true,
) {
    val backdrop = LocalLiquidGlassBackdrop.current
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    var selectedTypeIndex by rememberSaveable { mutableIntStateOf(0) } // 0=支出, 1=收入

    LaunchedEffect(selectedCategoryId, expenseCategories, incomeCategories) {
        when {
            selectedCategoryId == null -> Unit
            expenseCategories.any { it.id == selectedCategoryId } -> selectedTypeIndex = 0
            incomeCategories.any { it.id == selectedCategoryId } -> selectedTypeIndex = 1
        }
    }

    val shownCategories = if (selectedTypeIndex == 0) expenseCategories else incomeCategories
    val options = remember(shownCategories) {
        buildList {
            add("全部" to null)
            shownCategories.forEach { category ->
                add(category.name to category.id)
            }
        }
    }
    val selectedIndex = options.indexOfFirst { it.second == selectedCategoryId }
        .takeIf { it >= 0 } ?: 0
    val chipBounds = remember(options) { mutableStateMapOf<Int, ChipBounds>() }
    val selectedBounds = chipBounds[selectedIndex]
    val indicatorOffsetX by animateDpAsState(
        targetValue = with(density) { (selectedBounds?.xPx ?: 0f).toDp() },
        animationSpec = spring(stiffness = 520f, dampingRatio = 0.84f),
        label = "categoryIndicatorOffsetX",
    )
    val indicatorOffsetY by animateDpAsState(
        targetValue = with(density) { (selectedBounds?.yPx ?: 0f).toDp() },
        animationSpec = spring(stiffness = 520f, dampingRatio = 0.84f),
        label = "categoryIndicatorOffsetY",
    )
    val indicatorWidth by animateDpAsState(
        targetValue = with(density) { (selectedBounds?.widthPx ?: 0f).toDp() },
        animationSpec = spring(stiffness = 520f, dampingRatio = 0.84f),
        label = "categoryIndicatorWidth",
    )
    val indicatorHeight by animateDpAsState(
        targetValue = with(density) { (selectedBounds?.heightPx ?: 0f).toDp() },
        animationSpec = spring(stiffness = 520f, dampingRatio = 0.84f),
        label = "categoryIndicatorHeight",
    )
    val indicatorShape = RoundedCornerShape(999.dp)
    val indicatorColor = Color.White.copy(alpha = 0.2f)
    val indicatorBorderColor = Color.White.copy(alpha = 0.56f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (showHeader) {
            Row(
                modifier = Modifier.padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "分类筛选",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        LiquidGlassSegmentedControl(
            options = listOf("支出", "收入"),
            selectedIndex = selectedTypeIndex,
            onSelected = { index ->
                if (selectedTypeIndex != index) {
                    selectedTypeIndex = index
                    onCategorySelected(null)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 2.dp, vertical = 2.dp),
        ) {
            if (selectedBounds != null) {
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffsetX, y = indicatorOffsetY)
                        .width(indicatorWidth)
                        .height(indicatorHeight)
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
                                        lens(8f.dp.toPx(), 10f.dp.toPx())
                                    },
                                    highlight = { Highlight.Ambient.copy(alpha = 0.9f) },
                                    onDrawSurface = { drawRect(indicatorColor) },
                                )
                            },
                        )
                        .border(1.dp, indicatorBorderColor, indicatorShape),
                ) {
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                options.forEachIndexed { index, option ->
                    val (label, categoryId) = option
                    Box(
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInParent()
                            chipBounds[index] = ChipBounds(
                                xPx = position.x,
                                yPx = position.y,
                                widthPx = coordinates.size.width.toFloat(),
                                heightPx = coordinates.size.height.toFloat(),
                            )
                        },
                    ) {
                        LiquidGlassChoiceChip(
                            text = label,
                            selected = selectedIndex == index,
                            onClick = {
                                when {
                                    categoryId == null -> onCategorySelected(null)
                                    selectedCategoryId == categoryId -> onCategorySelected(null)
                                    else -> onCategorySelected(categoryId)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

private data class ChipBounds(
    val xPx: Float,
    val yPx: Float,
    val widthPx: Float,
    val heightPx: Float,
)

private fun currentMonthStartFromDevice(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun nextMonthStart(monthStart: Long): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = monthStart }
    cal.add(Calendar.MONTH, 1)
    return cal.timeInMillis
}
