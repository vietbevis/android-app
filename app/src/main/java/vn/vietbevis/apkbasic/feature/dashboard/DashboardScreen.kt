package vn.vietbevis.apkbasic.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vn.vietbevis.apkbasic.core.di.AppContainer
import vn.vietbevis.apkbasic.domain.model.Transaction
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.reporting.FinanceSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    appContainer: AppContainer,
    onOpenCapture: () -> Unit,
) {
    val viewModel = remember {
        DashboardViewModel(
            walletRepository = appContainer.walletRepository,
            categoryRepository = appContainer.categoryRepository,
            transactionRepository = appContainer.transactionRepository,
        )
    }
    val uiState by viewModel.uiState.collectAsState()

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
            Column {
                Text("Tổng quan", style = MaterialTheme.typography.headlineSmall)
                Text("Tháng ${uiState.monthRange.label}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedButton(onClick = viewModel::refresh, enabled = !uiState.isLoading) {
                Text("Tải lại")
            }
        }
        Spacer(Modifier.height(16.dp))
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.errorMessage != null -> Text(uiState.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
            uiState.summary != null -> DashboardSummary(
                summary = requireNotNull(uiState.summary),
                onOpenCapture = onOpenCapture,
            )
        }
    }
}

@Composable
private fun DashboardSummary(
    summary: FinanceSummary,
    onOpenCapture: () -> Unit,
) {
    if (summary.recentTransactions.isEmpty()) {
        EmptyDashboard(onOpenCapture)
        return
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricCard(title = "Thu", value = "+${summary.income.formatVnd()}", modifier = Modifier.weight(1f))
        MetricCard(title = "Chi", value = "-${summary.expense.formatVnd()}", modifier = Modifier.weight(1f))
    }
    Spacer(Modifier.height(12.dp))
        MetricCard(title = "Chênh lệch", value = summary.netChange.formatVnd(), modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(20.dp))
    SectionTitle("Số dư theo ví")
    summary.walletBalances.forEach {
        TwoColumnRow(label = it.wallet.name, value = it.balance.formatVnd())
    }
    Spacer(Modifier.height(20.dp))
    SectionTitle("Chi theo danh mục")
    summary.expenseByCategory.take(5).forEach {
        TwoColumnRow(label = it.category?.name ?: "Khác", value = it.amount.formatVnd())
    }
    Spacer(Modifier.height(20.dp))
    SectionTitle("Gần đây")
    summary.recentTransactions.forEach { transaction ->
        TransactionLine(transaction)
        HorizontalDivider()
    }
    Spacer(Modifier.height(16.dp))
    Button(onClick = onOpenCapture, modifier = Modifier.fillMaxWidth()) {
        Text("Chụp giao dịch mới")
    }
}

@Composable
private fun EmptyDashboard(onOpenCapture: () -> Unit) {
    Text("Chưa có giao dịch trong tháng này.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(12.dp))
    Button(onClick = onOpenCapture, modifier = Modifier.fillMaxWidth()) {
        Text("Tạo giao dịch đầu tiên")
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun TwoColumnRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label)
        Text(value, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun TransactionLine(transaction: Transaction) {
    val sign = if (transaction.type == TransactionType.EXPENSE) "-" else "+"
    val date = remember(transaction.occurredAtEpochMillis) {
        SimpleDateFormat("dd/MM HH:mm", Locale.forLanguageTag("vi-VN")).format(Date(transaction.occurredAtEpochMillis))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(transaction.note ?: if (transaction.type == TransactionType.EXPENSE) "Khoản chi" else "Khoản thu")
            Text(date, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Text("$sign${transaction.amount.formatVnd()}", style = MaterialTheme.typography.labelLarge)
    }
}
