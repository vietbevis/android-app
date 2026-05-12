package vn.vietbevis.apkbasic.feature.budgets

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vn.vietbevis.apkbasic.core.di.AppContainer
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.ui.components.CapCard
import vn.vietbevis.apkbasic.ui.theme.CapBackground
import vn.vietbevis.apkbasic.ui.theme.CapExpenseCoral
import vn.vietbevis.apkbasic.ui.theme.CapIncomeMint
import vn.vietbevis.apkbasic.ui.theme.CapPrimaryBlue
import vn.vietbevis.apkbasic.ui.theme.CapSurfaceHigh
import vn.vietbevis.apkbasic.ui.theme.CapTextSecondary

@Composable
fun BudgetsScreen(
    modifier: Modifier = Modifier,
    appContainer: AppContainer,
    userProfile: UserProfile,
) {
    val viewModel = remember(userProfile.id) {
        BudgetsViewModel(
            userProfile = userProfile,
            budgetRepository = appContainer.budgetRepository,
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
                Text("Ngân sách", style = MaterialTheme.typography.headlineMedium)
                Text("Dữ liệu đồng bộ trực tiếp từ Supabase", color = CapTextSecondary)
            }
        }
        item {
            CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Thêm ngân sách", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = uiState.nameInput,
                        onValueChange = viewModel::onNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tên ngân sách") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.amountInput,
                        onValueChange = viewModel::onAmountChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Số tiền ngân sách") },
                        singleLine = true,
                    )
                    Button(onClick = viewModel::createBudget, modifier = Modifier.fillMaxWidth()) {
                        Text("Tạo ngân sách tháng")
                    }
                }
            }
        }
        uiState.errorMessage?.let { message ->
            item { Text(message, color = MaterialTheme.colorScheme.error) }
        }
        uiState.infoMessage?.let { message ->
            item { Text(message, color = CapIncomeMint) }
        }
        when {
            uiState.isLoading -> item {
                Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.budgets.isEmpty() -> item {
                CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Chưa có ngân sách", style = MaterialTheme.typography.titleLarge)
                        Text("Tạo ngân sách đầu tiên để theo dõi mức chi hằng tháng bằng dữ liệu thật.", color = CapTextSecondary)
                    }
                }
            }
            else -> items(uiState.budgets, key = { it.budget.id }) { item ->
                BudgetCard(item = item, onArchive = { viewModel.archiveBudget(item.budget.id) })
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
}

@Composable
private fun BudgetCard(
    item: BudgetProgressItem,
    onArchive: () -> Unit,
) {
    val progress = item.progress
    CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(item.budget.name, style = MaterialTheme.typography.titleLarge)
                    Text("Hằng tháng · Tổng", color = CapTextSecondary)
                }
                TextButton(onClick = onArchive) {
                    Text("Lưu trữ")
                }
            }
            LinearProgressIndicator(
                progress = { (progress.percentSpent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = CapPrimaryBlue,
                trackColor = CapBackground,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${progress.percentSpent}% đã chi", color = CapTextSecondary)
                Text("Ngân sách ${progress.budget.formatVnd()}", color = CapTextSecondary)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BudgetValue("Đã chi", progress.spent.formatVnd(), CapExpenseCoral)
                BudgetValue("Còn lại", progress.remaining.formatVnd(), CapIncomeMint)
                BudgetValue("Mỗi ngày", progress.dailyAllowance.formatVnd(), CapPrimaryBlue)
            }
        }
    }
}

@Composable
private fun BudgetValue(title: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column {
        Text(title, color = CapTextSecondary)
        Text(value, color = color, style = MaterialTheme.typography.titleMedium)
    }
}
