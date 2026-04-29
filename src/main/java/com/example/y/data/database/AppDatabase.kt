package com.example.y.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.y.data.converter.Converters
import com.example.y.data.dao.AccountDao
import com.example.y.data.dao.CategoryDao
import com.example.y.data.dao.TransactionDao
import com.example.y.data.model.Account
import com.example.y.data.model.Category
import com.example.y.data.model.TransactionRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 应用数据库
 *
 * 架构决策：
 * - version = 1，后续用 autoMigrations / Migration 做版本演进
 * - exportSchema = true，schema JSON 导出到 app/schemas/，便于版本追溯
 * - TypeConverters 在 Database 级别注册，所有 DAO 自动可用
 * - 预置默认分类和账户通过 Callback.onCreate 写入
 */
@Database(
    entities = [
        TransactionRecord::class,
        Category::class,
        Account::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao

    companion object {
        const val DATABASE_NAME = "y_bookkeeping.db"

        /**
         * 构建数据库实例（由 Hilt Module 调用）
         */
        fun build(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(SeedDatabaseCallback())
                // 未来版本迁移示例：
                // .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                // 或使用自动迁移：
                // .addAutoMigrationSpec(...)
                .build()
        }
    }

    /**
     * 首次创建数据库时，插入预置的分类和账户数据
     */
    private class SeedDatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // 在 IO 线程中执行预置数据插入
            CoroutineScope(Dispatchers.IO).launch {
                val database = instance ?: return@launch
                database.categoryDao().insertAll(DefaultData.allDefaultCategories)
                database.accountDao().insertAll(DefaultData.defaultAccounts)
            }
        }
    }
}

/**
 * 持有单例引用，仅供 SeedDatabaseCallback 使用。
 * 正式实例由 Hilt 管理生命周期。
 */
private var instance: AppDatabase? = null

internal fun AppDatabase.Companion.buildAndHold(context: Context): AppDatabase {
    return build(context).also { instance = it }
}
