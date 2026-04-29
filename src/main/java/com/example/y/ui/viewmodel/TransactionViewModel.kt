package com.example.y.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.y.data.model.Account
import com.example.y.data.model.Category
import com.example.y.data.model.CategorySummary
import com.example.y.data.model.DailySummary
import com.example.y.data.model.RangeSummary
import com.example.y.data.model.TransactionRecord
import com.example.y.data.model.TransactionType
import com.example.y.data.model.TransactionWithDetails
import com.example.y.data.repository.AccountRepository
import com.example.y.data.repository.CategoryRepository
import com.example.y.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * 记账主 ViewModel
 *
 * - 通过 [dateRange] 驱动所有与时间相关的查询自动刷新
 * - 所有数据以 [StateFlow] 暴露给 Compose UI，配合 collectAsState() 使用
 * - 写操作在 viewModelScope 中执行，自动取消
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val categoryRepo: CategoryRepository,
    private val accountRepo: AccountRepository
) : ViewModel() {

    // ==================== 时间范围状态 ====================

    private val _dateRange = MutableStateFlow(currentMonthRange())
    val dateRange: StateFlow<Pair<Long, Long>> = _dateRange.asStateFlow()

    fun setDateRange(startDate: Long, endDate: Long) {
        _dateRange.value = startDate to endDate
    }

    // ==================== 分类筛选状态 ====================

    /** null 表示不筛选（显示全部），非 null 表示只显示该分类的交易 */
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    fun setCategoryFilter(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    // ==================== 交易列表（随时间范围 + 分类筛选自动刷新） ====================

    val transactions: StateFlow<List<TransactionWithDetails>> =
        combine(_dateRange, _selectedCategoryId) { range, catId -> Triple(range.first, range.second, catId) }
            .flatMapLatest { (start, end, catId) ->
                if (catId != null) {
                    transactionRepo.getByCategoryAndDateRange(catId, start, end)
                } else {
                    transactionRepo.getByDateRange(start, end)
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==================== 统计数据 ====================

    val rangeSummary: StateFlow<RangeSummary> =
        _dateRange.flatMapLatest { (start, end) ->
            transactionRepo.getRangeSummary(start, end)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            RangeSummary(0, 0, 0)
        )

    val expenseCategorySummary: StateFlow<List<CategorySummary>> =
        _dateRange.flatMapLatest { (start, end) ->
            transactionRepo.getCategorySummary(TransactionType.EXPENSE, start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategorySummary: StateFlow<List<CategorySummary>> =
        _dateRange.flatMapLatest { (start, end) ->
            transactionRepo.getCategorySummary(TransactionType.INCOME, start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailySummary: StateFlow<List<DailySummary>> =
        _dateRange.flatMapLatest { (start, end) ->
            transactionRepo.getDailySummary(start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==================== 分类 & 账户 ====================

    val expenseCategories: StateFlow<List<Category>> =
        categoryRepo.getActiveByType(TransactionType.EXPENSE)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategories: StateFlow<List<Category>> =
        categoryRepo.getActiveByType(TransactionType.INCOME)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<Account>> =
        accountRepo.getActiveAccounts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==================== 写操作 ====================

    fun addTransaction(record: TransactionRecord) {
        viewModelScope.launch {
            transactionRepo.insert(record)
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            categoryRepo.insert(category)
        }
    }

    fun updateTransaction(record: TransactionRecord) {
        viewModelScope.launch {
            transactionRepo.update(record.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            transactionRepo.deleteById(id)
        }
    }

    // ==================== 工具方法 ====================

    companion object {
        /** 获取当前月份的时间范围 [月初0点, 下月初0点) */
        fun currentMonthRange(): Pair<Long, Long> {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.add(Calendar.MONTH, 1)
            val end = cal.timeInMillis
            return start to end
        }
    }
}
