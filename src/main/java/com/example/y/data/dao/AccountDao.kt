package com.example.y.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.y.data.model.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<Account>): List<Long>

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): Account?

    /** 获取所有未归档账户 */
    @Query(
        """
        SELECT * FROM accounts 
        WHERE isArchived = 0 
        ORDER BY sortOrder ASC, id ASC
        """
    )
    fun getActiveAccounts(): Flow<List<Account>>

    /** 获取所有账户（含归档） */
    @Query("SELECT * FROM accounts ORDER BY sortOrder ASC, id ASC")
    fun getAll(): Flow<List<Account>>

    /** 获取默认账户 */
    @Query("SELECT * FROM accounts WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): Account?

    /** 检查该账户是否有关联交易（删除前校验） */
    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId")
    suspend fun getTransactionCount(accountId: Long): Int

    /** 按名称查找账户（如"微信"） */
    @Query("SELECT * FROM accounts WHERE name = :name AND isArchived = 0 LIMIT 1")
    suspend fun findByName(name: String): Account?
}
