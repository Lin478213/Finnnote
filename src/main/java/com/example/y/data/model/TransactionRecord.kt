package com.example.y.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 交易记录实体
 *
 * - [amount] 以"分"为单位存储，避免浮点精度问题（¥100.50 → 10050L）
 * - [date] 使用 epoch millis，便于区间查询与索引
 * - [extra] 预留 JSON 扩展字段，用于未来版本新增属性而无需修改表结构
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("categoryId"),
        Index("accountId"),
        Index("date"),
        Index("type")
    ]
)
data class TransactionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 金额，以"分"为单位 */
    val amount: Long,

    /** 交易类型，参见 [TransactionType] */
    val type: TransactionType,

    /** 分类 ID，可为空（分类被删除时置空） */
    val categoryId: Long? = null,

    /** 账户 ID */
    val accountId: Long,

    /** 备注 */
    val note: String = "",

    /** 交易发生的日期时间，epoch millis */
    val date: Long,

    /** 记录创建时间 */
    val createdAt: Long = System.currentTimeMillis(),

    /** 记录最后更新时间 */
    val updatedAt: Long = System.currentTimeMillis(),

    /** 预留扩展字段（JSON） */
    val extra: String = ""
)
