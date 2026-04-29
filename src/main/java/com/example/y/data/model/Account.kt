package com.example.y.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 账户实体（现金、银行卡、支付宝、微信等）
 *
 * - [initialBalance] 以"分"为单位，记录账户初始余额
 * - 实际余额 = initialBalance + 该账户所有收入 - 该账户所有支出
 */
@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 账户名称 */
    val name: String,

    /** Material Icon 名称 */
    val icon: String = "account_balance_wallet",

    /** ARGB 颜色值 */
    val color: Long = 0xFF2196F3,

    /** 初始余额，以"分"为单位 */
    val initialBalance: Long = 0,

    /** 排序权重 */
    val sortOrder: Int = 0,

    /** 是否为默认账户 */
    val isDefault: Boolean = false,

    /** 是否已归档 */
    val isArchived: Boolean = false,

    /** 创建时间 */
    val createdAt: Long = System.currentTimeMillis()
)
