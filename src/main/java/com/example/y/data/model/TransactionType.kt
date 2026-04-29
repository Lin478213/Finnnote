package com.example.y.data.model

/**
 * 交易类型枚举
 * - EXPENSE: 支出
 * - INCOME: 收入
 */
enum class TransactionType(val value: Int) {
    EXPENSE(0),
    INCOME(1);

    companion object {
        fun fromValue(value: Int): TransactionType =
            entries.first { it.value == value }
    }
}
