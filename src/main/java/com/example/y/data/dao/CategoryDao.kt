package com.example.y.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.y.data.model.Category
import com.example.y.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>): List<Long>

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): Category?

    /** 按类型获取所有未归档分类，排序显示 */
    @Query(
        """
        SELECT * FROM categories 
        WHERE type = :type AND isArchived = 0 
        ORDER BY sortOrder ASC, id ASC
        """
    )
    fun getActiveByType(type: TransactionType): Flow<List<Category>>

    /** 获取所有分类（含归档） */
    @Query("SELECT * FROM categories ORDER BY type ASC, sortOrder ASC, id ASC")
    fun getAll(): Flow<List<Category>>

    /** 获取默认分类 */
    @Query("SELECT * FROM categories WHERE isDefault = 1 AND type = :type LIMIT 1")
    suspend fun getDefault(type: TransactionType): Category?
}
