package com.example.y.ui.screen.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.y.data.model.Account
import com.example.y.ui.component.glass.LiquidGlassColumnPanel
import com.example.y.ui.component.glass.LiquidGlassPanel
import com.example.y.ui.component.glass.LiquidGlassTextFieldShape
import com.example.y.ui.component.glass.LiquidGlassTopBar
import com.example.y.ui.component.glass.liquidGlassOutlinedTextFieldColors
import com.example.y.ui.component.glass.liquidGlassPressEffect
import com.example.y.ui.theme.LocalLiquidGlassBackdrop
import com.example.y.ui.theme.AppLayout
import com.example.y.ui.util.formatAmount
import com.example.y.ui.viewmodel.AccountManagementViewModel
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import kotlinx.coroutines.flow.collectLatest
import java.math.BigDecimal
import java.math.RoundingMode

private val accountPresetColors = listOf(
    0xFF4CAF50,
    0xFF2196F3,
    0xFF1976D2,
    0xFFFF9800,
    0xFF9C27B0,
    0xFFE91E63,
    0xFF00BCD4,
    0xFF795548,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManagementScreen(
    onBack: () -> Unit,
    viewModel: AccountManagementViewModel = hiltViewModel(),
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val activeAccounts = remember(accounts) { accounts.filter { !it.isArchived } }
    val archivedAccounts = remember(accounts) { accounts.filter { it.isArchived } }
    val snackbarHostState = remember { SnackbarHostState() }
    val backInteraction = remember { MutableInteractionSource() }
    val addInteraction = remember { MutableInteractionSource() }

    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<Account?>(null) }
    var deletingAccount by remember { mutableStateOf<Account?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.messages.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            LiquidGlassTopBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("账户管理") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        interactionSource = backInteraction,
                        modifier = Modifier.liquidGlassPressEffect(
                            interactionSource = backInteraction,
                            pressedScale = 1.06f,
                        ),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingAccount = null
                    showEditDialog = true
                },
                interactionSource = addInteraction,
                modifier = Modifier.liquidGlassPressEffect(
                    interactionSource = addInteraction,
                    pressedScale = 1.06f,
                ),
            ) {
                Icon(Icons.Default.Add, contentDescription = "新增账户")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AppLayout.ScreenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(AppLayout.ScreenSectionSpacing),
        ) {
            item {
                SectionHeader(title = "可用账户 (${activeAccounts.size})")
            }
            item {
                if (activeAccounts.isEmpty()) {
                    EmptyStateCard(text = "暂无可用账户，点击右下角新增")
                } else {
                    AccountListPanel(
                        accounts = activeAccounts,
                        onEdit = {
                            editingAccount = it
                            showEditDialog = true
                        },
                        onSetDefault = { viewModel.setDefaultAccount(it.id) },
                        onToggleArchive = { viewModel.setArchived(it.id, archived = true) },
                        onDelete = { deletingAccount = it },
                    )
                }
            }

            item {
                SectionHeader(title = "已归档 (${archivedAccounts.size})")
            }
            item {
                if (archivedAccounts.isEmpty()) {
                    EmptyStateCard(text = "无归档账户")
                } else {
                    AccountListPanel(
                        accounts = archivedAccounts,
                        onEdit = {
                            editingAccount = it
                            showEditDialog = true
                        },
                        onSetDefault = { viewModel.setDefaultAccount(it.id) },
                        onToggleArchive = { viewModel.setArchived(it.id, archived = false) },
                        onDelete = { deletingAccount = it },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(AppLayout.BottomFabSpacer)) }
        }
    }

    if (showEditDialog) {
        AccountEditDialog(
            account = editingAccount,
            hasAnyDefault = accounts.any { !it.isArchived && it.isDefault },
            onDismiss = { showEditDialog = false },
            onConfirm = { name, initialBalance, color, setAsDefault ->
                val editing = editingAccount
                if (editing == null) {
                    viewModel.addAccount(
                        name = name,
                        initialBalance = initialBalance,
                        color = color,
                        setAsDefault = setAsDefault,
                    )
                } else {
                    viewModel.updateAccount(
                        accountId = editing.id,
                        name = name,
                        initialBalance = initialBalance,
                        color = color,
                        setAsDefault = setAsDefault,
                    )
                }
                showEditDialog = false
            },
        )
    }

    deletingAccount?.let { account ->
        val confirmInteraction = remember { MutableInteractionSource() }
        val cancelInteraction = remember { MutableInteractionSource() }
        AlertDialog(
            onDismissRequest = { deletingAccount = null },
            title = { Text("删除账户") },
            text = { Text("确认删除“${account.name}”？若有交易记录将无法删除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount(account.id)
                        deletingAccount = null
                    },
                    interactionSource = confirmInteraction,
                    modifier = Modifier.liquidGlassPressEffect(confirmInteraction, pressedScale = 1.06f),
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deletingAccount = null },
                    interactionSource = cancelInteraction,
                    modifier = Modifier.liquidGlassPressEffect(cancelInteraction, pressedScale = 1.06f),
                ) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            horizontal = AppLayout.SectionLabelHorizontalPadding,
            vertical = AppLayout.SectionLabelVerticalPadding,
        ),
    )
}

@Composable
private fun EmptyStateCard(text: String) {
    LiquidGlassPanel(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(AppLayout.PanelContentPadding),
        )
    }
}

@Composable
private fun AccountListPanel(
    accounts: List<Account>,
    onEdit: (Account) -> Unit,
    onSetDefault: (Account) -> Unit,
    onToggleArchive: (Account) -> Unit,
    onDelete: (Account) -> Unit,
) {
    LiquidGlassColumnPanel(
        modifier = Modifier.fillMaxWidth(),
    ) {
        accounts.forEachIndexed { index, account ->
            AccountRow(
                account = account,
                onEdit = { onEdit(account) },
                onSetDefault = { onSetDefault(account) },
                onToggleArchive = { onToggleArchive(account) },
                onDelete = { onDelete(account) },
            )
            if (index < accounts.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = AppLayout.PanelContentPadding),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f),
                )
            }
        }
    }
}

@Composable
private fun AccountRow(
    account: Account,
    onEdit: () -> Unit,
    onSetDefault: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val menuInteraction = remember { MutableInteractionSource() }
    val backdrop = LocalLiquidGlassBackdrop.current
    val menuShape = RoundedCornerShape(24.dp)
    val menuContainerColor = Color.White.copy(alpha = 0.92f)
    val menuBorderColor = Color.White.copy(alpha = 0.98f)
    val menuInnerBorderColor = Color.White.copy(alpha = 0.66f)
    val menuGradient = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.98f),
            Color.White.copy(alpha = 0.9f),
        )
    )
    val menuGlassModifier = if (backdrop == null) {
        Modifier.background(menuContainerColor, menuShape)
    } else {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { menuShape },
            effects = {
                vibrancy()
                blur(16f.dp.toPx())
                lens(18f.dp.toPx(), 24f.dp.toPx())
            },
            highlight = { Highlight.Default.copy(alpha = 1f) },
            onDrawSurface = { drawRect(menuContainerColor) },
        )
    }
        .shadow(
            elevation = 8.dp,
            shape = menuShape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.24f),
            spotColor = Color.Black.copy(alpha = 0.18f),
        )
        .background(menuGradient, menuShape)
        .drawWithContent {
            drawContent()
            drawRoundRect(
                color = Color.White.copy(alpha = 0.22f),
                topLeft = Offset(0f, 0f),
                size = Size(size.width, size.height * 0.48f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(120f, 120f),
            )
        }
    val menuItemColors = MenuDefaults.itemColors(
        textColor = MaterialTheme.colorScheme.onSurface,
        leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val hasSetDefaultAction = !account.isArchived && !account.isDefault
    val menuItemCount = if (hasSetDefaultAction) 4 else 3

    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(account.color)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = account.name.take(1),
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = account.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                )
                if (account.isDefault) {
                    Spacer(modifier = Modifier.size(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "默认",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        },
        supportingContent = {
            Text(
                text = buildString {
                    append("初始余额 ")
                    append(account.initialBalance.formatAmount())
                    if (account.isArchived) append(" · 已归档")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    interactionSource = menuInteraction,
                    modifier = Modifier.liquidGlassPressEffect(menuInteraction, pressedScale = 1.06f),
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更多")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = menuGlassModifier
                        .clip(menuShape)
                        .border(1.6.dp, menuBorderColor, menuShape)
                        .border(0.8.dp, menuInnerBorderColor, menuShape),
                    shape = menuShape,
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    border = BorderStroke(0.dp, Color.Transparent),
                ) {
                    DropdownMenuItem(
                        modifier = Modifier.clip(menuItemShape(0, menuItemCount)),
                        text = { Text("编辑") },
                        colors = menuItemColors,
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        },
                    )
                    if (hasSetDefaultAction) {
                        DropdownMenuItem(
                            modifier = Modifier.clip(menuItemShape(1, menuItemCount)),
                            text = { Text("设为默认") },
                            colors = menuItemColors,
                            onClick = {
                                menuExpanded = false
                                onSetDefault()
                            },
                        )
                    }
                    DropdownMenuItem(
                        modifier = Modifier.clip(
                            menuItemShape(
                                if (hasSetDefaultAction) 2 else 1,
                                menuItemCount,
                            )
                        ),
                        text = { Text(if (account.isArchived) "恢复账户" else "归档账户") },
                        colors = menuItemColors,
                        onClick = {
                            menuExpanded = false
                            onToggleArchive()
                        },
                    )
                    DropdownMenuItem(
                        modifier = Modifier.clip(
                            menuItemShape(
                                if (hasSetDefaultAction) 3 else 2,
                                menuItemCount,
                            )
                        ),
                        text = { Text("删除账户") },
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.error,
                            leadingIconColor = MaterialTheme.colorScheme.error,
                        ),
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                    )
                }
            }
        },
    )
}

@Composable
private fun AccountEditDialog(
    account: Account?,
    hasAnyDefault: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, initialBalance: Long, color: Long, setAsDefault: Boolean) -> Unit,
) {
    var name by remember(account) { mutableStateOf(account?.name.orEmpty()) }
    var amountText by remember(account) {
        mutableStateOf(if (account == null) "" else centsToInput(account.initialBalance))
    }
    var selectedColor by remember(account) { mutableStateOf(account?.color ?: accountPresetColors.first()) }
    var setAsDefault by remember(account, hasAnyDefault) {
        mutableStateOf(account?.isDefault ?: !hasAnyDefault)
    }
    var amountError by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val confirmInteraction = remember { MutableInteractionSource() }
    val cancelInteraction = remember { MutableInteractionSource() }
    val canConfirm = name.trim().isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (account == null) "新增账户" else "编辑账户") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 20) name = it },
                    singleLine = true,
                    label = { Text("账户名称") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = LiquidGlassTextFieldShape,
                    colors = liquidGlassOutlinedTextFieldColors(),
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        amountError = null
                    },
                    singleLine = true,
                    label = { Text("初始余额（元）") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = {
                        amountError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = LiquidGlassTextFieldShape,
                    colors = liquidGlassOutlinedTextFieldColors(),
                )
                Column {
                    Text(
                        text = "账户颜色",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        accountPresetColors.forEach { color ->
                            val interaction = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(color))
                                    .border(
                                        width = if (selectedColor == color) 2.dp else 1.dp,
                                        color = if (selectedColor == color) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        },
                                        shape = CircleShape,
                                    )
                                    .liquidGlassPressEffect(
                                        interactionSource = interaction,
                                        pressedScale = 1.1f,
                                    )
                                    .clickable(
                                        interactionSource = interaction,
                                        indication = null,
                                        onClick = { selectedColor = color },
                                    ),
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "设为默认账户",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Switch(
                        checked = setAsDefault,
                        onCheckedChange = { setAsDefault = it },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsed = parseAmountToCents(amountText)
                    if (parsed == null) {
                        amountError = "请输入正确金额，例如 123.45"
                        return@TextButton
                    }
                    onConfirm(name.trim(), parsed, selectedColor, setAsDefault)
                },
                enabled = canConfirm,
                interactionSource = confirmInteraction,
                modifier = Modifier.liquidGlassPressEffect(
                    interactionSource = confirmInteraction,
                    enabled = canConfirm,
                    pressedScale = 1.06f,
                ),
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                interactionSource = cancelInteraction,
                modifier = Modifier.liquidGlassPressEffect(cancelInteraction, pressedScale = 1.06f),
            ) {
                Text("取消")
            }
        },
    )
}

private fun parseAmountToCents(input: String): Long? {
    val normalized = input.trim().ifEmpty { "0" }
    return try {
        val decimal = normalized.toBigDecimal()
        if (decimal < BigDecimal.ZERO) return null
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

private fun menuItemShape(index: Int, total: Int): RoundedCornerShape {
    return when (index) {
        0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        total - 1 -> RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        else -> RoundedCornerShape(0.dp)
    }
}
