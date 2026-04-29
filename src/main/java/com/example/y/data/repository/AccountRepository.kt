package com.example.y.data.repository

import com.example.y.data.dao.AccountDao
import com.example.y.data.model.Account
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao
) {
    suspend fun insert(account: Account): Long =
        accountDao.insert(account)

    suspend fun update(account: Account) =
        accountDao.update(account)

    suspend fun delete(account: Account) =
        accountDao.delete(account)

    suspend fun getById(id: Long): Account? =
        accountDao.getById(id)

    fun getActiveAccounts(): Flow<List<Account>> =
        accountDao.getActiveAccounts()

    fun getAll(): Flow<List<Account>> =
        accountDao.getAll()

    suspend fun getDefault(): Account? =
        accountDao.getDefault()

    /** 按名称查找账户（如"微信"） */
    suspend fun findByName(name: String): Account? =
        accountDao.findByName(name)

    /** 获取默认账户（别名，供 Service 使用） */
    suspend fun getDefaultAccount(): Account? =
        accountDao.getDefault()

    /** 删除前检查是否有关联交易 */
    suspend fun canDelete(accountId: Long): Boolean =
        accountDao.getTransactionCount(accountId) == 0
}
