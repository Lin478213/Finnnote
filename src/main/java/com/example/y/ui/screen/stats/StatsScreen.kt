package com.example.y.ui.screen.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.y.data.model.CategorySummary
import com.example.y.ui.component.glass.LiquidGlassPanel
import com.example.y.ui.component.glass.LiquidGlassSegmentedControl
import com.example.y.ui.component.glass.LiquidGlassTopBar
import com.example.y.ui.component.glass.liquidGlassPressEffect
import com.example.y.ui.screen.common.IncomeSummaryCard
import com.example.y.ui.theme.AppLayout
import com.example.y.ui.util.formatAmount
import com.example.y.ui.util.formatYearMonth
import com.example.y.ui.viewmodel.TransactionViewModel
import java.util.Calendar

// 饼图色板
private val PieColors = listOf(
    Color(0xFFE91E63), Color(0xFF2196F3), Color(0xFFFF9800),
    Color(0xFF4CAF50), Color(0xFF9C27B0), Color(0xFF00BCD4),
    Color(0xFFF44336), Color(0xFF795548), Color(0xFF3F51B5),
    Color(0xFF607D8B), Color(0xFFFF5722), Color(0xFF009688),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: TransactionViewModel = hiltViewModel(),
) {
    val dateRange by viewModel.dateRange.collectAsState()
    val summary by viewModel.rangeSummary.collectAsState()
    val expenseCategories by viewModel.expenseCategorySummary.collectAsState()
    val incomeCategories by viewModel.incomeCategorySummary.collectAsState()
    val currentMonthStart = currentMonthStartFromDevice()
    val canGoNextMonth = dateRange.first < currentMonthStart
    val prevMonthInteraction = remember { MutableInteractionSource() }
    val nextMonthInteraction = remember { MutableInteractionSource() }

    LaunchedEffect(dateRange.first, currentMonthStart) {
        if (dateRange.first > currentMonthStart) {
            viewModel.setDateRange(
                startDate = currentMonthStart,
                endDate = nextMonthStart(currentMonthStart),
            )
        }
    }

    // 0 = 支出, 1 = 收入
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val currentCategories = if (selectedTab == 0) expenseCategories else incomeCategories
    val totalAmount = currentCategories.sumOf { it.totalAmount }

    Scaffold(
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
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(top = AppLayout.ScreenItemSpacing),
            verticalArrangement = Arrangement.spacedBy(AppLayout.ScreenItemSpacing),
        ) {
            // 收支总览卡片
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

            // 支出/收入切换
            item {
                LiquidGlassSegmentedControl(
                    options = listOf("支出", "收入"),
                    selectedIndex = selectedTab,
                    onSelected = { selectedTab = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = AppLayout.ScreenHorizontalPadding,
                            vertical = AppLayout.ScreenItemSpacing,
                        ),
                )
            }

            // 饼图
            if (currentCategories.isNotEmpty()) {
                item {
                    PieChart(
                        categories = currentCategories,
                        totalAmount = totalAmount,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 48.dp, vertical = 16.dp)
                            .aspectRatio(1f),
                    )
                }

                // 分类排行
                item {
                    Text(
                        text = "分类排行",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(
                            horizontal = AppLayout.ScreenHorizontalPadding,
                            vertical = AppLayout.SectionLabelVerticalPadding,
                        ),
                    )
                }

                itemsIndexed(currentCategories) { index, cat ->
                    CategoryRankItem(
                        rank = index + 1,
                        category = cat,
                        totalAmount = totalAmount,
                        color = PieColors[index % PieColors.size],
                    )
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "暂无数据",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(AppLayout.BottomContentSpacer)) }
        }
    }
}

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

@Composable
private fun PieChart(
    categories: List<CategorySummary>,
    totalAmount: Long,
    modifier: Modifier = Modifier,
) {
    val strokeWidth = 48.dp
    val segmentGapDegrees = if (categories.size > 1) 1.6f else 0f
    val edgeSofteningAlpha = 0.22f

    Canvas(modifier = modifier) {
        val diameter = size.minDimension
        val strokePx = strokeWidth.toPx()
        val topLeft = Offset(strokePx / 2, strokePx / 2)
        val arcSize = Size(diameter - strokePx, diameter - strokePx)
        var startAngle = -90f

        categories.forEachIndexed { index, cat ->
            val rawSweep = if (totalAmount > 0) (cat.totalAmount.toFloat() / totalAmount) * 360f else 0f
            val drawSweep = (rawSweep - segmentGapDegrees).coerceAtLeast(0f)
            if (drawSweep > 0f) {
                val segmentStart = startAngle + ((rawSweep - drawSweep) / 2f)
                val segmentColor = PieColors[index % PieColors.size]

                // 先画一层轻微外扩柔化边，降低锐利感
                drawArc(
                    color = segmentColor.copy(alpha = edgeSofteningAlpha),
                    startAngle = segmentStart,
                    sweepAngle = drawSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx + 3f, cap = StrokeCap.Round),
                )
                drawArc(
                    color = segmentColor,
                    startAngle = segmentStart,
                    sweepAngle = drawSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
            startAngle += rawSweep
        }
    }
}

@Composable
private fun CategoryRankItem(
    rank: Int,
    category: CategorySummary,
    totalAmount: Long,
    color: Color,
) {
    val percent = if (totalAmount > 0) category.totalAmount.toFloat() / totalAmount else 0f

    LiquidGlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = AppLayout.ScreenHorizontalPadding,
                vertical = AppLayout.ScreenItemSpacing / 2,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 排名
            Text(
                text = "$rank",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(24.dp),
            )

            // 色块
            Canvas(modifier = Modifier.size(12.dp)) {
                drawCircle(color = color)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 名称 + 进度条
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = category.categoryName ?: "未分类",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Text(
                        text = "%.1f%%".format(percent * 100),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { percent },
                    modifier = Modifier.fillMaxWidth(),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 金额
            Text(
                text = category.totalAmount.formatAmount(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
