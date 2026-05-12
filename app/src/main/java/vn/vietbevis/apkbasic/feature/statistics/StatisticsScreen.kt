package vn.vietbevis.apkbasic.feature.statistics

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vn.vietbevis.apkbasic.core.di.AppContainer
import vn.vietbevis.apkbasic.domain.reporting.FinanceSummary
import vn.vietbevis.apkbasic.ui.components.CapCard
import vn.vietbevis.apkbasic.ui.components.CapPillRow
import vn.vietbevis.apkbasic.ui.components.CapStatusPill
import vn.vietbevis.apkbasic.ui.theme.CapBackground
import vn.vietbevis.apkbasic.ui.theme.CapExpenseCoral
import vn.vietbevis.apkbasic.ui.theme.CapIncomeMint
import vn.vietbevis.apkbasic.ui.theme.CapPrimaryBlue
import vn.vietbevis.apkbasic.ui.theme.CapSurfaceHigh
import vn.vietbevis.apkbasic.ui.theme.CapTextSecondary

@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    appContainer: AppContainer,
) {
    val viewModel = remember {
        StatisticsViewModel(
            walletRepository = appContainer.walletRepository,
            categoryRepository = appContainer.categoryRepository,
            transactionRepository = appContainer.transactionRepository,
        )
    }
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CapBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Column(Modifier.padding(top = 20.dp)) {
                Text("Thống kê", style = MaterialTheme.typography.headlineMedium)
                CapPillRow(Modifier.padding(top = 16.dp)) {
                    CapStatusPill(text = "Tháng", selected = true)
                    CapStatusPill(text = "Năm", selected = false)
                    CapStatusPill(text = "Tất cả", selected = false)
                }
                Text(
                    text = "tháng ${uiState.monthRange.label}",
                    modifier = Modifier.padding(top = 18.dp).align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
        when {
            uiState.isLoading -> item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.errorMessage != null -> item {
                Text(uiState.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
            uiState.summary != null -> item {
                StatisticsContent(summary = requireNotNull(uiState.summary))
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
}

@Composable
private fun StatisticsContent(summary: FinanceSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StatCard("Thu nhập", summary.income.formatVnd(), CapIncomeMint, Modifier.weight(1f))
            StatCard("Chi tiêu", summary.expense.formatVnd(), CapExpenseCoral, Modifier.weight(1f))
        }
        StatCard("Số dư", summary.netChange.formatVnd(), if (summary.netChange.minorUnits >= 0) CapIncomeMint else CapExpenseCoral)
        CapPillRow {
            CapStatusPill("Danh mục", selected = true)
            CapStatusPill("Bản đồ", selected = false)
        }
        CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Chi tiêu theo danh mục", style = MaterialTheme.typography.titleLarge)
                val total = summary.expense.minorUnits.coerceAtLeast(1)
                if (summary.expenseByCategory.isEmpty()) {
                    Text("Chưa có dữ liệu chi tiêu.", color = CapTextSecondary)
                } else {
                    summary.expenseByCategory.forEach { categoryTotal ->
                        val fraction = categoryTotal.amount.minorUnits.toFloat() / total.toFloat()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(categoryTotal.category?.name ?: "Khác", modifier = Modifier.weight(1f))
                            Text(categoryTotal.amount.formatVnd(), color = CapExpenseCoral)
                        }
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = CapPrimaryBlue,
                            trackColor = CapBackground,
                        )
                    }
                }
            }
        }
        CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Bản đồ dữ liệu", style = MaterialTheme.typography.titleLarge)
                val located = summary.recentTransactions.filter { it.latitude != null && it.longitude != null }
                val missingLocation = summary.recentTransactions.size - located.size
                if (located.isEmpty()) {
                    Text(
                        "Tháng này có ${summary.recentTransactions.size} giao dịch thật, nhưng chưa giao dịch nào lưu vị trí.",
                        color = CapTextSecondary,
                    )
                } else {
                    Text("${located.size} giao dịch có vị trí · $missingLocation giao dịch chưa có vị trí", color = CapTextSecondary)
                    located.take(5).forEach { transaction ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(transaction.locationLabel ?: "Vị trí đã lưu", modifier = Modifier.weight(1f))
                            Text("${transaction.latitude}, ${transaction.longitude}", color = CapTextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    CapCard(modifier = modifier, containerColor = CapSurfaceHigh) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(34.dp)
                    .background(accent, RoundedCornerShape(17.dp)),
            )
            Text(title, color = CapTextSecondary)
            Text(value, style = MaterialTheme.typography.titleLarge, color = accent)
        }
    }
}
