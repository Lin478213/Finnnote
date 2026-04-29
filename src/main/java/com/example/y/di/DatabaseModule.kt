package com.example.y.di

import android.content.Context
import com.example.y.data.dao.AccountDao
import com.example.y.data.dao.CategoryDao
import com.example.y.data.dao.TransactionDao
import com.example.y.data.database.AppDatabase
import com.example.y.data.database.buildAndHold
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt 数据库依赖注入模块
 *
 * - AppDatabase 全局单例，跟随 Application 生命周期
 * - 各 DAO 从 Database 实例中获取，同样是单例
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.buildAndHold(context)
    }

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideAccountDao(database: AppDatabase): AccountDao {
        return database.accountDao()
    }
}
