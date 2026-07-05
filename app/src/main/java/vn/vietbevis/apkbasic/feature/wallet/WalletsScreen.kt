package vn.vietbevis.apkbasic.feature.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.vietbevis.apkbasic.R
import vn.vietbevis.apkbasic.core.di.AppContainer
import vn.vietbevis.apkbasic.domain.model.Category
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.model.Wallet
import vn.vietbevis.apkbasic.domain.model.WalletType
import vn.vietbevis.apkbasic.ui.components.CapCard
import vn.vietbevis.apkbasic.ui.components.CapPillRow
import vn.vietbevis.apkbasic.ui.components.CapStatusPill
import vn.vietbevis.apkbasic.ui.theme.APKBasicTheme

@Composable
fun WalletsScreen(
    modifier: Modifier = Modifier,
    appContainer: AppContainer,
    userProfile: UserProfile,
) {
    val viewModel = remember(userProfile.id) {
        WalletsViewModel(
            userProfile = userProfile,
            walletRepository = appContainer.walletRepository,
            categoryRepository = appContainer.categoryRepository,
        )
    }
    val uiState by viewModel.uiState.collectAsState()
    var archiveWallet by remember { mutableStateOf<Wallet?>(null) }
    var archiveCategory by remember { mutableStateOf<Category?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.wallets_header), style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = viewModel::refresh, enabled = !uiState.isLoading) {
                Text(stringResource(R.string.common_reload))
            }
        }
        Spacer(Modifier.height(12.dp))
        when {
            uiState.isLoading -> CircularProgressIndicator()
            else -> WalletsContent(
                uiState = uiState,
                onWalletNameChange = viewModel::onWalletNameChange,
                onWalletTypeChange = viewModel::onWalletTypeChange,
                onCreateWallet = viewModel::createWallet,
                onArchiveWallet = { archiveWallet = it },
                onCategoryNameChange = viewModel::onCategoryNameChange,
                onCategoryTypeChange = viewModel::onCategoryTypeChange,
                onCreateCategory = viewModel::createCategory,
                onArchiveCategory = { archiveCategory = it },
            )
        }
    }

    archiveWallet?.let { wallet ->
        ConfirmArchiveDialog(
            title = stringResource(R.string.wallets_archive_wallet_title),
            body = stringResource(R.string.wallets_archive_wallet_body, wallet.name),
            onConfirm = {
                viewModel.archiveWallet(wallet.id)
                archiveWallet = null
            },
            onDismiss = { archiveWallet = null },
        )
    }
    archiveCategory?.let { category ->
        ConfirmArchiveDialog(
            title = stringResource(R.string.wallets_archive_category_title),
            body = stringResource(R.string.wallets_archive_category_body, category.name),
            onConfirm = {
                viewModel.archiveCategory(category.id)
                archiveCategory = null
            },
            onDismiss = { archiveCategory = null },
        )
    }
}

@Composable
private fun WalletsContent(
    uiState: WalletsUiState,
    onWalletNameChange: (String) -> Unit,
    onWalletTypeChange: (WalletType) -> Unit,
    onCreateWallet: () -> Unit,
    onArchiveWallet: (Wallet) -> Unit,
    onCategoryNameChange: (String) -> Unit,
    onCategoryTypeChange: (TransactionType) -> Unit,
    onCreateCategory: () -> Unit,
    onArchiveCategory: (Category) -> Unit,
) {
    uiState.errorMessage?.let {
        Text(it, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
    }
    uiState.infoMessage?.let {
        Text(it, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
    }

    Text(stringResource(R.string.wallets_create_wallet), style = MaterialTheme.typography.titleMedium)
    OutlinedTextField(
        value = uiState.walletNameInput,
        onValueChange = onWalletNameChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.wallets_wallet_name)) },
        singleLine = true,
    )
    TypeChips(
        values = WalletType.entries,
        selected = uiState.walletTypeInput,
        label = { it.displayName() },
        onSelected = onWalletTypeChange,
    )
    Button(onClick = onCreateWallet, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.wallets_add_wallet))
    }
    Spacer(Modifier.height(16.dp))
    Text(stringResource(R.string.wallets_list_header), style = MaterialTheme.typography.titleMedium)
    if (uiState.wallets.isEmpty()) {
        Text(stringResource(R.string.wallets_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        uiState.wallets.forEach { wallet ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(wallet.name, style = MaterialTheme.typography.titleSmall)
                    Text(wallet.type.displayName(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { onArchiveWallet(wallet) }) {
                    Text(stringResource(R.string.wallets_archive_action))
                }
            }
            HorizontalDivider()
        }
    }

    Spacer(Modifier.height(24.dp))
    Text(stringResource(R.string.wallets_create_category), style = MaterialTheme.typography.titleMedium)
    OutlinedTextField(
        value = uiState.categoryNameInput,
        onValueChange = onCategoryNameChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.wallets_category_name)) },
        singleLine = true,
    )
    TypeChips(
        values = TransactionType.entries,
        selected = uiState.categoryTypeInput,
        label = { if (it == TransactionType.EXPENSE) stringResource(R.string.dashboard_expense) else stringResource(R.string.dashboard_income) },
        onSelected = onCategoryTypeChange,
    )
    Button(onClick = onCreateCategory, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.wallets_add_category))
    }
    Spacer(Modifier.height(16.dp))
    Text(stringResource(R.string.wallets_category_list_header), style = MaterialTheme.typography.titleMedium)
    uiState.categories.groupBy { it.transactionType }.forEach { (type, categories) ->
        Text(if (type == TransactionType.EXPENSE) stringResource(R.string.dashboard_expense) else stringResource(R.string.dashboard_income), style = MaterialTheme.typography.titleSmall)
        categories.forEach { category ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(category.name)
                    if (category.isDefault) {
                        Text(stringResource(R.string.wallets_default_label), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
                TextButton(onClick = { onArchiveCategory(category) }) {
                    Text(stringResource(R.string.wallets_archive_action))
                }
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun <T> TypeChips(
    values: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        values.forEach { value ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelected(value) },
                label = { Text(label(value)) },
            )
        }
    }
}

@Composable
private fun ConfirmArchiveDialog(
    title: String,
    body: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.wallets_archive_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun WalletType.displayName(): String = when (this) {
    WalletType.CASH -> stringResource(R.string.wallet_type_cash)
    WalletType.BANK -> stringResource(R.string.wallet_type_bank)
    WalletType.E_WALLET -> stringResource(R.string.wallet_type_ewallet)
    WalletType.OTHER -> stringResource(R.string.wallet_type_other)
}

@Preview(showBackground = true, name = "Wallets Screen")
@Composable
private fun WalletsScreenPreview() {
    APKBasicTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text("Ví và danh mục", style = MaterialTheme.typography.headlineMedium)
            CapPillRow {
                CapStatusPill("Tiền mặt", selected = true)
                CapStatusPill("Ngân hàng", selected = false)
            }
            CapCard(modifier = Modifier.fillMaxWidth(), containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tạo ví", style = MaterialTheme.typography.titleLarge)
                    Text("Tên ví", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Loại ví", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            listOf("Tiền mặt" to "Ví mặc định", "Momo" to "Ví điện tử").forEach { (name, type) ->
                CapCard(modifier = Modifier.fillMaxWidth(), containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.titleMedium)
                            Text(type, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("Lưu trữ", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
