package com.example.y.data.model

/**
 * 按分类汇总的统计结果（用于饼图等）
 */
data class CategorySummary(
    val categoryId: Long?,
    val categoryName: String?,
    val categoryIcon: String?,
    val categoryColor: Long?,
    val totalAmount: Long,
    val transactionCount: Int
)

/**
 * 按日汇总的收支统计（用于柱状图等）
 */
data class DailySummary(
    /** 当天 0 点的 epoch millis */
    val dayTimestamp: Long,
    val totalIncome: Long,
    val totalExpense: Long
)

/**
 * 指定时间范围内的收支总览
 */
data class RangeSummary(
    val totalIncome: Long,
    val totalExpense: Long,
    val transactionCount: Int
)
