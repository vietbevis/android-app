package vn.vietbevis.apkbasic.feature.transactions

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import vn.vietbevis.apkbasic.domain.model.Transaction
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.ui.components.SnapListItem
import vn.vietbevis.apkbasic.ui.components.SnapSectionHeader
import vn.vietbevis.apkbasic.ui.theme.APKBasicTheme

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen(
    modifier: Modifier = Modifier,
    appContainer: AppContainer,
    onEditTransaction: (Transaction) -> Unit = {},
) {
    val viewModel = remember {
        TransactionsViewModel(
            walletRepository = appContainer.walletRepository,
            categoryRepository = appContainer.categoryRepository,
            transactionRepository = appContainer.transactionRepository,
        )
    }
    val uiState by viewModel.uiState.collectAsState()
    var pendingDelete by remember { mutableStateOf<Transaction?>(null) }

    Log.v("TRANSACTIONVIEW", uiState.transactions.toString()?: "NULL ROI");

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(stringResource(R.string.transactions_header), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.dashboard_month_label, uiState.monthRange.label), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedButton(onClick = viewModel::refresh, enabled = !uiState.isLoading) {
                Text(stringResource(R.string.common_reload))
            }
        }
        Spacer(Modifier.height(12.dp))
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.errorMessage != null -> Text(uiState.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
            uiState.transactions.isEmpty() -> EmptyTransactions()
            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.transactions, key = { it.id }) { transaction ->
                    val walletName = uiState.wallets.firstOrNull { it.id == transaction.walletId }?.name ?: stringResource(R.string.transactions_unknown_wallet)
                    val category = uiState.categories.firstOrNull { it.id == transaction.categoryId }
                    val isExpense = transaction.type == TransactionType.EXPENSE
                    val sign = if (isExpense) "-" else "+"

                    val supabaseUrl = vn.vietbevis.apkbasic.BuildConfig.SUPABASE_URL.removeSuffix("/")
                    val imageUrl = transaction.photoPath?.let { path ->
                        if (path.startsWith("http")) path else "$supabaseUrl/storage/v1/object/public/transaction-photos/${path.removePrefix("/")}"
                    }

                    SnapListItem(
                        title = category?.name ?: if (isExpense) stringResource(R.string.transaction_expense_label) else stringResource(R.string.transaction_income_label),
                        subtitle = listOfNotNull(walletName, transaction.note).joinToString(" · "),
                        trailingTitle = "$sign${transaction.amount.formatVnd()}",
                        trailingSubtitle = if (isExpense) stringResource(R.string.transactions_action_edit) else stringResource(R.string.transaction_income_type),
                        iconText = category?.name ?: if (isExpense) "C" else "T",
                        imageUrl = imageUrl,
                        iconContainerColor = if (isExpense) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                        onClick = { onEditTransaction(transaction) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    pendingDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.transactions_delete_title)) },
            text = { Text(stringResource(R.string.transactions_delete_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(transaction.id)
                        pendingDelete = null
                    },
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun EmptyTransactions() {
    Text(stringResource(R.string.transactions_empty_month), color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Preview(showBackground = true, name = "Transactions Screen")
@Composable
private fun TransactionsScreenPreview() {
    APKBasicTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SnapSectionHeader(title = stringResource(R.string.transactions_header), actionText = stringResource(R.string.common_reload))
            Text(stringResource(R.string.dashboard_month_label, "05/26"), color = MaterialTheme.colorScheme.onSurfaceVariant)
            SnapListItem(
                title = "Ăn uống",
                subtitle = "Tiền mặt · 15/05/2026 12:30",
                trailingTitle = "-120.000 đ",
                trailingSubtitle = "Có ảnh",
            )
            SnapListItem(
                title = "Lương",
                subtitle = "Ngân hàng · 01/05/2026 09:00",
                trailingTitle = "+8.000.000 đ",
                trailingSubtitle = "Thu nhập",
            )
        }
    }
}
