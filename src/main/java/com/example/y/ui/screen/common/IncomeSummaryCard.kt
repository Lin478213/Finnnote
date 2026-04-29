package com.example.y.ui.screen.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.y.ui.component.glass.LiquidGlassPanel
import com.example.y.ui.theme.ExpenseRed
import com.example.y.ui.theme.IncomeGreen
import com.example.y.ui.util.formatAmount

/**
 * 统一的收入支出结余展示卡片
 * 用于账单页面和统计页面保持一致的样式
 */
@Composable
fun IncomeSummaryCard(
    income: Long,
    expense: Long,
    modifier: Modifier = Modifier,
) {
    LiquidGlassPanel(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SummaryColumn(label = "收入", amount = income, color = IncomeGreen)
            SummaryColumn(label = "支出", amount = expense, color = ExpenseRed)
            SummaryColumn(
                label = "结余",
                amount = income - expense,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SummaryColumn(
    label: String,
    amount: Long,
    color: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = amount.formatAmount(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}
