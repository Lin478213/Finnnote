package com.example.y.data.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * 交易记录 + 关联的分类 & 账户信息（一次查询获取完整展示所需数据）
 *
 * 使用 Room 的 @Relation 自动处理关联查询，DAO 方法需标注 @Transaction。
 */
data class TransactionWithDetails(
    @Embedded
    val transaction: TransactionRecord,

    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: Category?,

    @Relation(parentColumn = "accountId", entityColumn = "id")
    val account: Account
)
