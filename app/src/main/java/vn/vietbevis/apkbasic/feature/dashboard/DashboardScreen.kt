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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.vietbevis.apkbasic.R
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
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SnapSectionHeader(title = stringResource(R.string.dashboard_overview), actionText = stringResource(R.string.dashboard_reload), onAction = viewModel::refresh)
        Text(stringResource(R.string.dashboard_month_label, uiState.monthRange.label), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
        when {
            uiState.isLoading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            uiState.errorMessage != null -> SnapMessageCard(stringResource(R.string.dashboard_error_title), uiState.errorMessage.orEmpty())
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
            title = stringResource(R.string.dashboard_empty_title),
            body = stringResource(R.string.dashboard_empty_body),
            actionText = stringResource(R.string.dashboard_action_scan),
            onAction = onOpenCapture,
        )
        return
    }
    SnapSummaryBanner(label = stringResource(R.string.dashboard_balance), amount = summary.netChange.formatVnd(), meta = stringResource(R.string.dashboard_this_month))
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        DashboardMetric(stringResource(R.string.dashboard_income), "+${summary.income.formatVnd()}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        DashboardMetric(stringResource(R.string.dashboard_expense), "-${summary.expense.formatVnd()}", MaterialTheme.colorScheme.error, Modifier.weight(1f))
    }
    SnapSectionHeader(title = stringResource(R.string.dashboard_recent))
    summary.recentTransactions.take(6).forEach { transaction ->
        TransactionLine(transaction)
    }
    SnapPrimaryButton(text = stringResource(R.string.dashboard_action_new_capture), onClick = onOpenCapture, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun DashboardMetric(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    vn.vietbevis.apkbasic.ui.components.SnapColoredBanner(modifier = modifier, containerColor = color) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            Text(value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
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
        title = transaction.note ?: if (transaction.type == TransactionType.EXPENSE) stringResource(R.string.transaction_expense_label) else stringResource(R.string.transaction_income_label),
        subtitle = date,
        trailingTitle = "$sign${transaction.amount.formatVnd()}",
        trailingSubtitle = if (transaction.type == TransactionType.EXPENSE) stringResource(R.string.transaction_expense_type) else stringResource(R.string.transaction_income_type),
    )
}

@Preview(showBackground = true)
@Composable
private fun DashboardLoadingPreview() {
    APKBasicTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            SnapSectionHeader(title = stringResource(R.string.dashboard_overview), actionText = stringResource(R.string.dashboard_reload))
            SnapMessageCard(stringResource(R.string.dashboard_empty_title), stringResource(R.string.dashboard_empty_body))
        }
    }
}
