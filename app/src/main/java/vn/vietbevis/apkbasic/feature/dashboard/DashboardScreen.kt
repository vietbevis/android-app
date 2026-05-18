package vn.vietbevis.apkbasic.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.vietbevis.apkbasic.core.di.AppContainer
import vn.vietbevis.apkbasic.domain.model.Transaction
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.reporting.FinanceSummary
import vn.vietbevis.apkbasic.ui.components.SnapListItem
import vn.vietbevis.apkbasic.ui.components.SnapMessageCard
import vn.vietbevis.apkbasic.ui.components.SnapPrimaryButton
import vn.vietbevis.apkbasic.ui.components.SnapSectionHeader
import vn.vietbevis.apkbasic.ui.components.SnapSummaryBanner
import vn.vietbevis.apkbasic.ui.theme.APKBasicTheme
import vn.vietbevis.apkbasic.ui.theme.SnapBlue
import vn.vietbevis.apkbasic.ui.theme.SnapCoral
import vn.vietbevis.apkbasic.ui.theme.SnapCream
import vn.vietbevis.apkbasic.ui.theme.SnapMint
import vn.vietbevis.apkbasic.ui.theme.SnapNavy
import vn.vietbevis.apkbasic.ui.theme.SnapSlate
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
            .background(SnapCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SnapSectionHeader(title = "Tổng quan", actionText = "Tải lại", onAction = viewModel::refresh)
        Text("Tháng ${uiState.monthRange.label}", color = SnapSlate, style = MaterialTheme.typography.bodyLarge)
        when {
            uiState.isLoading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SnapCoral)
            }
            uiState.errorMessage != null -> SnapMessageCard("Không tải được tổng quan", uiState.errorMessage.orEmpty())
            uiState.summary != null -> DashboardSummary(requireNotNull(uiState.summary), onOpenCapture)
        }
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun DashboardSummary(
    summary: FinanceSummary,
    onOpenCapture: () -> Unit,
) {
    if (summary.recentTransactions.isEmpty()) {
        SnapMessageCard(
            title = "Chưa có giao dịch",
            body = "Tạo giao dịch đầu tiên để SnapChi bắt đầu tổng hợp số dư và chi tiêu.",
            actionText = "Quét biên lai",
            onAction = onOpenCapture,
        )
        return
    }
    SnapSummaryBanner(label = "Số dư", amount = summary.netChange.formatVnd(), meta = "Tháng này")
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        DashboardMetric("Thu", "+${summary.income.formatVnd()}", SnapMint, Modifier.weight(1f))
        DashboardMetric("Chi", "-${summary.expense.formatVnd()}", SnapBlue, Modifier.weight(1f))
    }
    SnapSectionHeader(title = "Gần đây")
    summary.recentTransactions.take(6).forEach { transaction ->
        TransactionLine(transaction)
    }
    SnapPrimaryButton(text = "Chụp giao dịch mới", onClick = onOpenCapture, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun DashboardMetric(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    vn.vietbevis.apkbasic.ui.components.SnapColoredBanner(modifier = modifier, containerColor = color) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = SnapSlate, style = MaterialTheme.typography.labelMedium)
            Text(value, color = SnapNavy, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun TransactionLine(transaction: Transaction) {
    val sign = if (transaction.type == TransactionType.EXPENSE) "-" else "+"
    val date = remember(transaction.occurredAtEpochMillis) {
        SimpleDateFormat("dd/MM HH:mm", Locale.forLanguageTag("vi-VN")).format(Date(transaction.occurredAtEpochMillis))
    }
    SnapListItem(
        title = transaction.note ?: if (transaction.type == TransactionType.EXPENSE) "Khoản chi" else "Khoản thu",
        subtitle = date,
        trailingTitle = "$sign${transaction.amount.formatVnd()}",
        trailingSubtitle = if (transaction.type == TransactionType.EXPENSE) "Chi tiêu" else "Thu nhập",
    )
}

@Preview(showBackground = true)
@Composable
private fun DashboardLoadingPreview() {
    APKBasicTheme {
        Column(Modifier.background(SnapCream).padding(16.dp)) {
            SnapSectionHeader(title = "Tổng quan", actionText = "Tải lại")
            SnapMessageCard("Chưa có giao dịch", "Tạo giao dịch đầu tiên để bắt đầu.")
        }
    }
}
