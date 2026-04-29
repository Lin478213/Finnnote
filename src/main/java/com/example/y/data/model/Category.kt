package com.example.y.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 分类实体
 *
 * - 每个分类绑定一个 [TransactionType]（支出 / 收入分开管理）
 * - [icon] 存储 Material Icon 名称，UI 层通过名称映射到实际 Icon
 * - [color] 存储 ARGB 色值
 */
@Entity(
    tableName = "categories",
    indices = [Index("type")]
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 分类名称 */
    val name: String,

    /** Material Icon 名称 */
    val icon: String = "category",

    /** ARGB 颜色值 */
    val color: Long = 0xFF4CAF50,

    /** 分类适用的交易类型 */
    val type: TransactionType,

    /** 排序权重，越小越靠前 */
    val sortOrder: Int = 0,

    /** 是否为系统默认分类 */
    val isDefault: Boolean = false,

    /** 是否已归档（软删除） */
    val isArchived: Boolean = false
)
