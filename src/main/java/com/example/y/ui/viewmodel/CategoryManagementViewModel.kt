package com.example.y.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.y.data.model.Category
import com.example.y.data.model.TransactionType
import com.example.y.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val categoryRepo: CategoryRepository,
) : ViewModel() {

    val categories: StateFlow<List<Category>> =
        categoryRepo.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages = _messages.asSharedFlow()

    fun addCategory(
        name: String,
        color: Long,
        type: TransactionType,
    ) {
        viewModelScope.launch {
            val normalizedName = name.trim()
            if (normalizedName.isBlank()) {
                _messages.tryEmit("分类名称不能为空")
                return@launch
            }
            val exists = categories.value.any {
                it.type == type && it.name.equals(normalizedName, ignoreCase = true) && !it.isArchived
            }
            if (exists) {
                _messages.tryEmit("该类型下已存在同名分类")
                return@launch
            }
            val nextSort = categories.value
                .filter { it.type == type }
                .maxOfOrNull { it.sortOrder }
                ?.plus(1) ?: 0

            categoryRepo.insert(
                Category(
                    name = normalizedName,
                    color = color,
                    type = type,
                    sortOrder = nextSort,
                ),
            )
            _messages.tryEmit("分类已创建")
        }
    }

    fun updateCategory(
        categoryId: Long,
        name: String,
        color: Long,
    ) {
        viewModelScope.launch {
            val old = categories.value.find { it.id == categoryId } ?: return@launch
            val normalizedName = name.trim()
            if (normalizedName.isBlank()) {
                _messages.tryEmit("分类名称不能为空")
                return@launch
            }
            val exists = categories.value.any {
                it.id != old.id &&
                    it.type == old.type &&
                    it.name.equals(normalizedName, ignoreCase = true) &&
                    !it.isArchived
            }
            if (exists) {
                _messages.tryEmit("该类型下已存在同名分类")
                return@launch
            }

            categoryRepo.update(
                old.copy(
                    name = normalizedName,
                    color = color,
                ),
            )
            _messages.tryEmit("分类已更新")
        }
    }

    fun setArchived(categoryId: Long, archived: Boolean) {
        viewModelScope.launch {
            val current = categories.value
            val category = current.find { it.id == categoryId } ?: return@launch
            if (category.isDefault) {
                _messages.tryEmit("默认分类不能归档")
                return@launch
            }

            if (archived) {
                val activeCount = current.count { it.type == category.type && !it.isArchived }
                if (!category.isArchived && activeCount <= 1) {
                    _messages.tryEmit("至少保留一个可用分类")
                    return@launch
                }
                categoryRepo.update(category.copy(isArchived = true))
                _messages.tryEmit("分类已归档")
            } else {
                categoryRepo.update(category.copy(isArchived = false))
                _messages.tryEmit("分类已恢复")
            }
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            val current = categories.value
            val category = current.find { it.id == categoryId } ?: return@launch
            if (category.isDefault) {
                _messages.tryEmit("默认分类不能删除")
                return@launch
            }

            val activeCount = current.count { it.type == category.type && !it.isArchived }
            if (!category.isArchived && activeCount <= 1) {
                _messages.tryEmit("至少保留一个可用分类")
                return@launch
            }

            categoryRepo.delete(category)
            _messages.tryEmit("分类已删除，历史账单将显示为未分类")
        }
    }
}
