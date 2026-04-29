package com.example.y.data.repository

import com.example.y.data.dao.CategoryDao
import com.example.y.data.model.Category
import com.example.y.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    suspend fun insert(category: Category): Long =
        categoryDao.insert(category)

    suspend fun update(category: Category) =
        categoryDao.update(category)

    suspend fun delete(category: Category) =
        categoryDao.delete(category)

    suspend fun getById(id: Long): Category? =
        categoryDao.getById(id)

    fun getActiveByType(type: TransactionType): Flow<List<Category>> =
        categoryDao.getActiveByType(type)

    fun getAll(): Flow<List<Category>> =
        categoryDao.getAll()

    suspend fun getDefault(type: TransactionType): Category? =
        categoryDao.getDefault(type)
}
