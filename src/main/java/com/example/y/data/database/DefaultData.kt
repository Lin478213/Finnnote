package com.example.y.data.database

import com.example.y.data.model.Account
import com.example.y.data.model.Category
import com.example.y.data.model.TransactionType

/**
 * 首次安装时的预置数据
 *
 * 分类参照国内主流记账 App 的常见分类，图标名称对应 Material Symbols。
 */
object DefaultData {

    /** 默认账户 */
    val defaultAccounts = listOf(
        Account(
            name = "现金",
            icon = "wallet",
            color = 0xFF4CAF50,
            isDefault = true,
            sortOrder = 0
        ),
        Account(
            name = "银行卡",
            icon = "account_balance",
            color = 0xFF2196F3,
            sortOrder = 1
        ),
        Account(
            name = "支付宝",
            icon = "account_balance_wallet",
            color = 0xFF1976D2,
            sortOrder = 2
        ),
        Account(
            name = "微信",
            icon = "chat",
            color = 0xFF4CAF50,
            sortOrder = 3
        )
    )

    /** 默认支出分类 */
    val defaultExpenseCategories = listOf(
        Category(name = "餐饮", icon = "restaurant", color = 0xFFE91E63, type = TransactionType.EXPENSE, sortOrder = 0, isDefault = true),
        Category(name = "交通", icon = "directions_bus", color = 0xFF2196F3, type = TransactionType.EXPENSE, sortOrder = 1),
        Category(name = "购物", icon = "shopping_cart", color = 0xFFFF9800, type = TransactionType.EXPENSE, sortOrder = 2),
        Category(name = "住房", icon = "home", color = 0xFF795548, type = TransactionType.EXPENSE, sortOrder = 3),
        Category(name = "娱乐", icon = "sports_esports", color = 0xFF9C27B0, type = TransactionType.EXPENSE, sortOrder = 4),
        Category(name = "医疗", icon = "local_hospital", color = 0xFFF44336, type = TransactionType.EXPENSE, sortOrder = 5),
        Category(name = "教育", icon = "school", color = 0xFF3F51B5, type = TransactionType.EXPENSE, sortOrder = 6),
        Category(name = "通讯", icon = "phone_android", color = 0xFF009688, type = TransactionType.EXPENSE, sortOrder = 7),
        Category(name = "服饰", icon = "checkroom", color = 0xFFE040FB, type = TransactionType.EXPENSE, sortOrder = 8),
        Category(name = "日用", icon = "local_mall", color = 0xFFFF5722, type = TransactionType.EXPENSE, sortOrder = 9),
        Category(name = "其他", icon = "more_horiz", color = 0xFF607D8B, type = TransactionType.EXPENSE, sortOrder = 99),
    )

    /** 默认收入分类 */
    val defaultIncomeCategories = listOf(
        Category(name = "工资", icon = "payments", color = 0xFF4CAF50, type = TransactionType.INCOME, sortOrder = 0, isDefault = true),
        Category(name = "奖金", icon = "emoji_events", color = 0xFFFF9800, type = TransactionType.INCOME, sortOrder = 1),
        Category(name = "兼职", icon = "work", color = 0xFF2196F3, type = TransactionType.INCOME, sortOrder = 2),
        Category(name = "理财", icon = "trending_up", color = 0xFF00BCD4, type = TransactionType.INCOME, sortOrder = 3),
        Category(name = "红包", icon = "redeem", color = 0xFFF44336, type = TransactionType.INCOME, sortOrder = 4),
        Category(name = "其他", icon = "more_horiz", color = 0xFF607D8B, type = TransactionType.INCOME, sortOrder = 99),
    )

    val allDefaultCategories = defaultExpenseCategories + defaultIncomeCategories
}
