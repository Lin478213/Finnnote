package com.example.y.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.y.data.model.Account
import com.example.y.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountManagementViewModel @Inject constructor(
    private val accountRepo: AccountRepository,
) : ViewModel() {

    val accounts: StateFlow<List<Account>> =
        accountRepo.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages = _messages.asSharedFlow()

    fun addAccount(
        name: String,
        initialBalance: Long,
        color: Long,
        setAsDefault: Boolean,
    ) {
        viewModelScope.launch {
            val normalizedName = name.trim()
            if (normalizedName.isBlank()) {
                _messages.tryEmit("账户名称不能为空")
                return@launch
            }
            val duplicated = accountRepo.findByName(normalizedName)
            if (duplicated != null) {
                _messages.tryEmit("已存在同名账户")
                return@launch
            }

            val current = accounts.value
            val nextSort = (current.maxOfOrNull { it.sortOrder } ?: -1) + 1
            val shouldDefault = setAsDefault || current.none { !it.isArchived && it.isDefault }
            if (shouldDefault) {
                current.filter { it.isDefault }.forEach { account ->
                    accountRepo.update(account.copy(isDefault = false))
                }
            }

            accountRepo.insert(
                Account(
                    name = normalizedName,
                    color = color,
                    initialBalance = initialBalance,
                    sortOrder = nextSort,
                    isDefault = shouldDefault,
                )
            )

            _messages.tryEmit("账户已创建")
        }
    }

    fun updateAccount(
        accountId: Long,
        name: String,
        initialBalance: Long,
        color: Long,
        setAsDefault: Boolean,
    ) {
        viewModelScope.launch {
            val current = accounts.value
            val old = current.find { it.id == accountId } ?: return@launch
            val normalizedName = name.trim()
            if (normalizedName.isBlank()) {
                _messages.tryEmit("账户名称不能为空")
                return@launch
            }
            val duplicated = accountRepo.findByName(normalizedName)
            if (duplicated != null && duplicated.id != old.id) {
                _messages.tryEmit("已存在同名账户")
                return@launch
            }

            accountRepo.update(
                old.copy(
                    name = normalizedName,
                    initialBalance = initialBalance,
                    color = color,
                    isArchived = false,
                )
            )

            if (setAsDefault) {
                setDefaultInternal(accountId)
            }

            _messages.tryEmit("账户已更新")
        }
    }

    fun setDefaultAccount(accountId: Long) {
        viewModelScope.launch {
            val account = accounts.value.find { it.id == accountId }
            if (account == null) return@launch
            if (account.isArchived) {
                _messages.tryEmit("归档账户不能设为默认")
                return@launch
            }
            setDefaultInternal(accountId)
            _messages.tryEmit("已设为默认账户")
        }
    }

    fun setArchived(accountId: Long, archived: Boolean) {
        viewModelScope.launch {
            val current = accounts.value
            val account = current.find { it.id == accountId } ?: return@launch

            if (archived) {
                val activeAccounts = current.filter { !it.isArchived }
                if (activeAccounts.size <= 1) {
                    _messages.tryEmit("至少保留一个可用账户")
                    return@launch
                }
                val fallbackDefaultId = activeAccounts.firstOrNull { it.id != accountId }?.id
                accountRepo.update(account.copy(isArchived = true, isDefault = false))
                if (account.isDefault && fallbackDefaultId != null) {
                    setDefaultInternal(fallbackDefaultId)
                }
                _messages.tryEmit("账户已归档")
            } else {
                accountRepo.update(account.copy(isArchived = false))
                val hasDefault = current.any { !it.isArchived && it.isDefault }
                if (!hasDefault) {
                    setDefaultInternal(accountId)
                }
                _messages.tryEmit("账户已恢复")
            }
        }
    }

    fun deleteAccount(accountId: Long) {
        viewModelScope.launch {
            val current = accounts.value
            val account = current.find { it.id == accountId } ?: return@launch
            if (!accountRepo.canDelete(accountId)) {
                _messages.tryEmit("该账户有关联交易，不能删除。请先归档。")
                return@launch
            }

            val activeCount = current.count { !it.isArchived }
            if (!account.isArchived && activeCount <= 1) {
                _messages.tryEmit("至少保留一个可用账户")
                return@launch
            }

            val fallbackDefaultId = current.firstOrNull { it.id != accountId && !it.isArchived }?.id
            accountRepo.delete(account)
            if (account.isDefault && fallbackDefaultId != null) {
                setDefaultInternal(fallbackDefaultId)
            }
            _messages.tryEmit("账户已删除")
        }
    }

    private suspend fun setDefaultInternal(accountId: Long) {
        accounts.value.forEach { account ->
            val shouldDefault = account.id == accountId
            val shouldArchived = if (shouldDefault) false else account.isArchived
            if (account.isDefault != shouldDefault || account.isArchived != shouldArchived) {
                accountRepo.update(
                    account.copy(
                        isDefault = shouldDefault,
                        isArchived = shouldArchived,
                    )
                )
            }
        }
    }
}
