package com.example.y.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.y.data.model.CategorySummary
import com.example.y.data.model.DailySummary
import com.example.y.data.model.RangeSummary
import com.example.y.data.model.TransactionRecord
import com.example.y.data.model.TransactionType
import com.example.y.data.model.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

/**
 * 交易记录 DAO
 *
 * 设计原则：
 * - 列表 / 统计类查询返回 [Flow]，数据变更时自动通知 UI
 * - 写操作使用 suspend（在协程中执行，不阻塞主线程）
 * - 时间范围查询基于 [date] 索引，高效执行
 */
@Dao
interface TransactionDao {

    // ==================== 写操作 ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: TransactionRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<TransactionRecord>): List<Long>

    @Update
    suspend fun update(record: TransactionRecord)

    @Delete
    suspend fun delete(record: TransactionRecord)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    // ==================== 单条查询 ====================

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getByIdWithDetails(id: Long): Flow<TransactionWithDetails?>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionRecord?

    // ==================== 列表查询（带关联信息） ====================

    /** 全部交易，按日期倒序 */
    @Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllWithDetails(): Flow<List<TransactionWithDetails>>

    /** 指定时间范围内的交易 */
    @Transaction
    @Query(
        """
        SELECT * FROM transactions 
        WHERE date >= :startDate AND date < :endDate 
        ORDER BY date DESC
        """
    )
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionWithDetails>>

    /** 按类型 + 时间范围查询 */
    @Transaction
    @Query(
        """
        SELECT * FROM transactions 
        WHERE type = :type AND date >= :startDate AND date < :endDate 
        ORDER BY date DESC
        """
    )
    fun getByTypeAndDateRange(
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionWithDetails>>

    /** 按分类查询 */
    @Transaction
    @Query(
        """
        SELECT * FROM transactions 
        WHERE categoryId = :categoryId 
        ORDER BY date DESC
        """
    )
    fun getByCategoryId(categoryId: Long): Flow<List<TransactionWithDetails>>

    /** 按分类 + 时间范围查询 */
    @Transaction
    @Query(
        """
        SELECT * FROM transactions 
        WHERE categoryId = :categoryId AND date >= :startDate AND date < :endDate 
        ORDER BY date DESC
        """
    )
    fun getByCategoryAndDateRange(
        categoryId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionWithDetails>>

    /** 按账户查询 */
    @Transaction
    @Query(
        """
        SELECT * FROM transactions 
        WHERE accountId = :accountId 
        ORDER BY date DESC
        """
    )
    fun getByAccountId(accountId: Long): Flow<List<TransactionWithDetails>>

    /** 分页查询（配合 LazyColumn） */
    @Transaction
    @Query(
        """
        SELECT * FROM transactions 
        WHERE date >= :startDate AND date < :endDate 
        ORDER BY date DESC 
        LIMIT :limit OFFSET :offset
        """
    )
    fun getPagedByDateRange(
        startDate: Long,
        endDate: Long,
        limit: Int,
        offset: Int
    ): Flow<List<TransactionWithDetails>>

    // ==================== 统计查询 ====================

    /** 时间范围内的收支总览 */
    @Query(
        """
        SELECT 
            COALESCE(SUM(CASE WHEN type = 1 THEN amount ELSE 0 END), 0) AS totalIncome,
            COALESCE(SUM(CASE WHEN type = 0 THEN amount ELSE 0 END), 0) AS totalExpense,
            COUNT(*) AS transactionCount
        FROM transactions 
        WHERE date >= :startDate AND date < :endDate
        """
    )
    fun getRangeSummary(startDate: Long, endDate: Long): Flow<RangeSummary>

    /** 按分类汇总（用于饼图） */
    @Query(
        """
        SELECT 
            t.categoryId,
            c.name AS categoryName,
            c.icon AS categoryIcon,
            c.color AS categoryColor,
            SUM(t.amount) AS totalAmount,
            COUNT(*) AS transactionCount
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id
        WHERE t.type = :type 
            AND t.date >= :startDate 
            AND t.date < :endDate
        GROUP BY t.categoryId
        ORDER BY totalAmount DESC
        """
    )
    fun getCategorySummary(
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): Flow<List<CategorySummary>>

    /**
     * 按天汇总收支（用于柱状图）
     *
     * 将 epoch millis 按天分组：date / 86400000 得到天数编号，
     * 再乘回 86400000 得到当天 0 点的 epoch millis。
     */
    @Query(
        """
        SELECT 
            (date / 86400000) * 86400000 AS dayTimestamp,
            COALESCE(SUM(CASE WHEN type = 1 THEN amount ELSE 0 END), 0) AS totalIncome,
            COALESCE(SUM(CASE WHEN type = 0 THEN amount ELSE 0 END), 0) AS totalExpense
        FROM transactions
        WHERE date >= :startDate AND date < :endDate
        GROUP BY date / 86400000
        ORDER BY dayTimestamp ASC
        """
    )
    fun getDailySummary(startDate: Long, endDate: Long): Flow<List<DailySummary>>

    /** 指定账户的交易总额（用于计算实际余额） */
    @Query(
        """
        SELECT COALESCE(
            SUM(CASE WHEN type = 1 THEN amount ELSE -amount END), 0
        )
        FROM transactions 
        WHERE accountId = :accountId
        """
    )
    fun getAccountTransactionSum(accountId: Long): Flow<Long>

    /** 交易记录总条数 */
    @Query("SELECT COUNT(*) FROM transactions")
    fun getTotalCount(): Flow<Int>
}
