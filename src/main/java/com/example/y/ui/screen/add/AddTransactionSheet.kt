package com.example.y.ui.screen.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.y.data.model.Category
import com.example.y.data.model.TransactionRecord
import com.example.y.data.model.TransactionType
import com.example.y.data.model.TransactionWithDetails
import com.example.y.ui.component.glass.LiquidGlassChoiceChip
import com.example.y.ui.component.glass.LiquidGlassPanel
import com.example.y.ui.component.glass.LiquidGlassSegmentedControl
import com.example.y.ui.component.glass.LiquidGlassTextFieldShape
import com.example.y.ui.component.glass.liquidGlassOutlinedTextFieldColors
import com.example.y.ui.component.glass.liquidGlassPressEffect
import com.example.y.ui.util.formatDate
import com.example.y.ui.viewmodel.TransactionViewModel
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionSheet(
    onDismiss: () -> Unit,
    editingTransaction: TransactionWithDetails? = null,
    viewModel: TransactionViewModel = hiltViewModel(),
) {
    val editingRecord = editingTransaction?.transaction
    val isEditing = editingRecord != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    // 表单状态
    var amountText by rememberSaveable(editingRecord?.id) {
        mutableStateOf(
            editingRecord?.amount?.let { centsToInput(it) } ?: "",
        )
    }
    var selectedType by rememberSaveable(editingRecord?.id) {
        mutableStateOf(editingRecord?.type ?: TransactionType.EXPENSE)
    }
    var selectedCategoryId by rememberSaveable(editingRecord?.id) {
        mutableStateOf(editingRecord?.categoryId)
    }
    var selectedAccountId by rememberSaveable(editingRecord?.id) {
        mutableStateOf(editingRecord?.accountId)
    }
    var note by rememberSaveable(editingRecord?.id) {
        mutableStateOf(editingRecord?.note.orEmpty())
    }
    var selectedDate by rememberSaveable(editingRecord?.id) {
        mutableLongStateOf(editingRecord?.date ?: System.currentTimeMillis())
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var accountMenuExpanded by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    val dateIconButtonInteraction = remember { MutableInteractionSource() }
    val saveButtonInteraction = remember { MutableInteractionSource() }
    val canSave = amountText.isNotBlank() && parseAmountToCents(amountText) != null && selectedAccountId != null

    val currentCategories = if (selectedType == TransactionType.EXPENSE) expenseCategories else incomeCategories

    // 首次选中默认账户
    if (selectedAccountId == null && accounts.isNotEmpty()) {
        selectedAccountId = accounts.firstOrNull { it.isDefault }?.id ?: accounts.first().id
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
    ) {
        LiquidGlassPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 14.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // 标题
                Text(
                    text = if (isEditing) "编辑账单" else "记一笔",
                    style = MaterialTheme.typography.headlineSmall,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 收入/支出切换 — M3 SegmentedButton
                LiquidGlassSegmentedControl(
                    options = listOf("支出", "收入"),
                    selectedIndex = if (selectedType == TransactionType.EXPENSE) 0 else 1,
                    onSelected = { index ->
                        selectedType = if (index == 0) TransactionType.EXPENSE else TransactionType.INCOME
                        selectedCategoryId = null
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 金额输入
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newValue ->
                        // 只允许数字和一个小数点，最多两位小数
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = newValue
                        }
                    },
                    label = { Text("金额") },
                    prefix = { Text("¥") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = LiquidGlassTextFieldShape,
                    colors = liquidGlassOutlinedTextFieldColors(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 分类选择 — M3 FilterChip in FlowRow
                Text(
                    text = "分类",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    currentCategories.forEach { category ->
                        LiquidGlassChoiceChip(
                            text = category.name,
                            selected = selectedCategoryId == category.id,
                            onClick = { selectedCategoryId = category.id },
                        )
                    }
                    // 新建分类按钮
                    LiquidGlassChoiceChip(
                        text = "+",
                        selected = false,
                        onClick = { showAddCategoryDialog = true },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 日期选择 + 账户选择（同一行）
                Row(modifier = Modifier.fillMaxWidth()) {
                    // 日期
                    OutlinedTextField(
                        value = selectedDate.formatDate(showYear = true),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("日期") },
                        trailingIcon = {
                            IconButton(
                                onClick = { showDatePicker = true },
                                interactionSource = dateIconButtonInteraction,
                                modifier = Modifier.liquidGlassPressEffect(
                                    interactionSource = dateIconButtonInteraction,
                                    pressedScale = 1.06f,
                                ),
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "选择日期")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = LiquidGlassTextFieldShape,
                        colors = liquidGlassOutlinedTextFieldColors(),
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // 账户下拉 — M3 ExposedDropdownMenuBox
                    ExposedDropdownMenuBox(
                        expanded = accountMenuExpanded,
                        onExpandedChange = { accountMenuExpanded = it },
                        modifier = Modifier.weight(1f),
                    ) {
                        OutlinedTextField(
                            value = accounts.find { it.id == selectedAccountId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("账户") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountMenuExpanded)
                            },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            singleLine = true,
                            shape = LiquidGlassTextFieldShape,
                            colors = liquidGlassOutlinedTextFieldColors(),
                        )
                        ExposedDropdownMenu(
                            expanded = accountMenuExpanded,
                            onDismissRequest = { accountMenuExpanded = false },
                        ) {
                            accounts.forEach { account ->
                                DropdownMenuItem(
                                    text = { Text(account.name) },
                                    onClick = {
                                        selectedAccountId = account.id
                                        accountMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 备注
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（选填）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = LiquidGlassTextFieldShape,
                    colors = liquidGlassOutlinedTextFieldColors(),
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 保存按钮 — M3 Filled Button
                Button(
                    onClick = {
                        val amountCents = parseAmountToCents(amountText) ?: return@Button
                        if (selectedAccountId != null) {
                            if (isEditing) {
                                viewModel.updateTransaction(
                                    editingRecord.copy(
                                        amount = amountCents,
                                        type = selectedType,
                                        categoryId = selectedCategoryId,
                                        accountId = selectedAccountId!!,
                                        note = note.trim(),
                                        date = selectedDate,
                                    ),
                                )
                            } else {
                                viewModel.addTransaction(
                                    TransactionRecord(
                                        amount = amountCents,
                                        type = selectedType,
                                        categoryId = selectedCategoryId,
                                        accountId = selectedAccountId!!,
                                        note = note.trim(),
                                        date = selectedDate,
                                    ),
                                )
                            }
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassPressEffect(
                            interactionSource = saveButtonInteraction,
                            enabled = canSave,
                            pressedScale = 1.04f,
                        ),
                    interactionSource = saveButtonInteraction,
                    enabled = canSave,
                ) {
                    Text(if (isEditing) "更新" else "保存")
                }

                if (isEditing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "可在账单列表三点菜单中删除此记录",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // 日期选择器弹窗 — M3 DatePickerDialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        val dateConfirmInteraction = remember { MutableInteractionSource() }
        val dateDismissInteraction = remember { MutableInteractionSource() }
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate = it }
                        showDatePicker = false
                    },
                    interactionSource = dateConfirmInteraction,
                    modifier = Modifier.liquidGlassPressEffect(dateConfirmInteraction, pressedScale = 1.06f),
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    interactionSource = dateDismissInteraction,
                    modifier = Modifier.liquidGlassPressEffect(dateDismissInteraction, pressedScale = 1.06f),
                ) {
                    Text("取消")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 新建分类对话框
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            type = selectedType,
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { category ->
                viewModel.addCategory(category)
                showAddCategoryDialog = false
            },
        )
    }
}

private fun parseAmountToCents(input: String): Long? {
    val normalized = input.trim().ifEmpty { "0" }
    return try {
        val decimal = normalized.toBigDecimal()
        if (decimal <= BigDecimal.ZERO) return null
        decimal
            .setScale(2, RoundingMode.HALF_UP)
            .movePointRight(2)
            .longValueExact()
    } catch (_: Throwable) {
        null
    }
}

private fun centsToInput(cents: Long): String {
    val decimal = BigDecimal(cents).movePointLeft(2).stripTrailingZeros()
    return if (decimal.compareTo(BigDecimal.ZERO) == 0) "0" else decimal.toPlainString()
}

/** 预置颜色供用户选择 */
private val categoryColors = listOf(
    0xFFE91E63, 0xFFF44336, 0xFFFF5722, 0xFFFF9800,
    0xFFFFC107, 0xFF4CAF50, 0xFF009688, 0xFF00BCD4,
    0xFF2196F3, 0xFF3F51B5, 0xFF673AB7, 0xFF9C27B0,
    0xFF795548, 0xFF607D8B,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCategoryDialog(
    type: TransactionType,
    onDismiss: () -> Unit,
    onConfirm: (Category) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(categoryColors.first()) }
    val createButtonInteraction = remember { MutableInteractionSource() }
    val cancelButtonInteraction = remember { MutableInteractionSource() }
    val canCreate = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (type == TransactionType.EXPENSE) "新建支出分类" else "新建收入分类")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 10) name = it },
                    label = { Text("分类名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = LiquidGlassTextFieldShape,
                    colors = liquidGlassOutlinedTextFieldColors(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "选择颜色",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(80.dp),
                ) {
                    items(categoryColors) { color ->
                        val isSelected = color == selectedColor
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .then(
                                    if (isSelected) Modifier.border(
                                        2.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                        CircleShape,
                                    ) else Modifier,
                                )
                                .clickable { selectedColor = color },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            Category(
                                name = name.trim(),
                                type = type,
                                color = selectedColor,
                                sortOrder = 50, // 用户自建分类排中间
                            ),
                        )
                    }
                },
                enabled = canCreate,
                interactionSource = createButtonInteraction,
                modifier = Modifier.liquidGlassPressEffect(
                    interactionSource = createButtonInteraction,
                    enabled = canCreate,
                    pressedScale = 1.06f,
                ),
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                interactionSource = cancelButtonInteraction,
                modifier = Modifier.liquidGlassPressEffect(cancelButtonInteraction, pressedScale = 1.06f),
            ) {
                Text("取消")
            }
        },
    )
}
