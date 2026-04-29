package com.example.y.data.repository

import com.example.y.data.dao.TransactionDao
import com.example.y.data.model.CategorySummary
import com.example.y.data.model.DailySummary
import com.example.y.data.model.RangeSummary
import com.example.y.data.model.TransactionRecord
import com.example.y.data.model.TransactionType
import com.example.y.data.model.TransactionWithDetails
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 交易记录仓库
 *
 * 职责：
 * - 为 ViewModel 提供干净的数据访问 API
 * - 封装 DAO 调用，可在此层添加业务校验 / 缓存策略
 * - 所有方法均线程安全（suspend / Flow）
 */
@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    // ==================== 写操作 ====================

    suspend fun insert(record: TransactionRecord): Long =
        transactionDao.insert(record)

    suspend fun update(record: TransactionRecord) =
        transactionDao.update(record)

    suspend fun delete(record: TransactionRecord) =
        transactionDao.delete(record)

    suspend fun deleteById(id: Long) =
        transactionDao.deleteById(id)

    // ==================== 查询 ====================

    fun getByIdWithDetails(id: Long): Flow<TransactionWithDetails?> =
        transactionDao.getByIdWithDetails(id)

    suspend fun getById(id: Long): TransactionRecord? =
        transactionDao.getById(id)

    fun getAllWithDetails(): Flow<List<TransactionWithDetails>> =
        transactionDao.getAllWithDetails()

    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionWithDetails>> =
        transactionDao.getByDateRange(startDate, endDate)

    fun getByTypeAndDateRange(
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionWithDetails>> =
        transactionDao.getByTypeAndDateRange(type, startDate, endDate)

    fun getByCategoryId(categoryId: Long): Flow<List<TransactionWithDetails>> =
        transactionDao.getByCategoryId(categoryId)

    fun getByCategoryAndDateRange(
        categoryId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionWithDetails>> =
        transactionDao.getByCategoryAndDateRange(categoryId, startDate, endDate)

    fun getByAccountId(accountId: Long): Flow<List<TransactionWithDetails>> =
        transactionDao.getByAccountId(accountId)

    fun getPagedByDateRange(
        startDate: Long,
        endDate: Long,
        limit: Int,
        offset: Int
    ): Flow<List<TransactionWithDetails>> =
        transactionDao.getPagedByDateRange(startDate, endDate, limit, offset)

    // ==================== 统计 ====================

    fun getRangeSummary(startDate: Long, endDate: Long): Flow<RangeSummary> =
        transactionDao.getRangeSummary(startDate, endDate)

    fun getCategorySummary(
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): Flow<List<CategorySummary>> =
        transactionDao.getCategorySummary(type, startDate, endDate)

    fun getDailySummary(startDate: Long, endDate: Long): Flow<List<DailySummary>> =
        transactionDao.getDailySummary(startDate, endDate)

    fun getAccountTransactionSum(accountId: Long): Flow<Long> =
        transactionDao.getAccountTransactionSum(accountId)
}
