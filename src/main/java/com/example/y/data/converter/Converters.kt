package com.example.y.data.converter

import androidx.room.TypeConverter
import com.example.y.data.model.TransactionType

/**
 * Room TypeConverters
 *
 * 负责在 Room 无法直接存储的 Kotlin 类型与 SQLite 支持的基础类型之间做转换。
 */
class Converters {

    @TypeConverter
    fun fromTransactionType(type: TransactionType): Int = type.value

    @TypeConverter
    fun toTransactionType(value: Int): TransactionType = TransactionType.fromValue(value)
}
