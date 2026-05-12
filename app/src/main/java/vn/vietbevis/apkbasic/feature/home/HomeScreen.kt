package vn.vietbevis.apkbasic.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.vietbevis.apkbasic.core.di.AppContainer
import vn.vietbevis.apkbasic.domain.model.Category
import vn.vietbevis.apkbasic.domain.model.Transaction
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.model.Wallet
import vn.vietbevis.apkbasic.ui.components.CapCard
import vn.vietbevis.apkbasic.ui.components.CapPillRow
import vn.vietbevis.apkbasic.ui.components.CapStatusPill
import vn.vietbevis.apkbasic.ui.theme.CapBackground
import vn.vietbevis.apkbasic.ui.theme.CapDivider
import vn.vietbevis.apkbasic.ui.theme.CapExpenseCoral
import vn.vietbevis.apkbasic.ui.theme.CapIncomeMint
import vn.vietbevis.apkbasic.ui.theme.CapPrimaryBlue
import vn.vietbevis.apkbasic.ui.theme.CapSurfaceHigh
import vn.vietbevis.apkbasic.ui.theme.CapTextSecondary

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    appContainer: AppContainer,
    userProfile: UserProfile,
    onOpenCapture: () -> Unit,
) {
    val viewModel = remember {
        HomeViewModel(
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
            HomeHeader(
                userProfile = userProfile,
                monthExpense = uiState.monthExpense.formatVnd(),
                onRefresh = viewModel::refresh,
            )
        }
        item {
            CapPillRow {
                CapStatusPill("Ngày", selected = true)
                CapStatusPill("Tháng", selected = false)
            }
        }
        item {
            SummaryRow(
                expense = uiState.monthExpense.formatVnd(),
                income = uiState.monthIncome.formatVnd(),
            )
        }
        item {
            WalletFilters(
                wallets = uiState.wallets,
                selectedWalletId = uiState.selectedWalletId,
                onSelected = viewModel::selectWallet,
            )
        }
        item {
            MonthStrip(
                label = "tháng ${uiState.monthRange.label}",
                calendarDays = uiState.calendarDays,
                onDaySelected = viewModel::selectDay,
            )
        }
        item {
            DaySummary(
                selectedDay = uiState.selectedDayOfMonth,
                expense = uiState.dayExpense.formatVnd(),
                income = uiState.dayIncome.formatVnd(),
            )
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
            uiState.dayTransactions.isEmpty() -> item {
                EmptyDay(onOpenCapture = onOpenCapture)
            }
            else -> items(uiState.dayTransactions, key = { it.id }) { transaction ->
                TransactionCard(
                    transaction = transaction,
                    wallet = uiState.wallets.firstOrNull { it.id == transaction.walletId },
                    category = uiState.categories.firstOrNull { it.id == transaction.categoryId },
                )
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
}

@Composable
private fun HomeHeader(
    userProfile: UserProfile,
    monthExpense: String,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Chào buổi sáng", style = MaterialTheme.typography.titleMedium, color = CapTextSecondary)
            Text(
                text = userProfile.displayName?.takeIf { it.isNotBlank() } ?: "CapMoney",
                style = MaterialTheme.typography.headlineMedium,
            )
            Surface(
                modifier = Modifier.padding(top = 8.dp),
                color = CapExpenseCoral.copy(alpha = 0.18f),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(
                    text = "Đã chi $monthExpense tháng này",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = CapExpenseCoral,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        TextButton(onClick = onRefresh) {
            Text("Tải lại")
        }
    }
}

@Composable
private fun SummaryRow(expense: String, income: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        MetricTile(
            title = "Chi",
            value = expense,
            accent = CapExpenseCoral,
            modifier = Modifier.weight(1f),
        )
        MetricTile(
            title = "Thu",
            value = income,
            accent = CapIncomeMint,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    CapCard(modifier = modifier, containerColor = CapSurfaceHigh) {
        Column(Modifier.padding(18.dp)) {
            Surface(color = accent, shape = CircleShape) {
                Box(Modifier.padding(8.dp))
            }
            Text(value, modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.titleLarge)
            Text(title, color = CapTextSecondary)
        }
    }
}

@Composable
private fun WalletFilters(
    wallets: List<Wallet>,
    selectedWalletId: String?,
    onSelected: (String?) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FilterPill("Tất cả", selectedWalletId == null) { onSelected(null) }
        wallets.take(5).forEach { wallet ->
            FilterPill(wallet.name, selectedWalletId == wallet.id) { onSelected(wallet.id) }
        }
    }
}

@Composable
private fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = if (selected) CapPrimaryBlue else CapSurfaceHigh,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, if (selected) CapPrimaryBlue else CapDivider),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun MonthStrip(
    label: String,
    calendarDays: List<HomeCalendarDay>,
    onDaySelected: (Int) -> Unit,
) {
    CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(label, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
            FlowRow(
                maxItemsInEachRow = 7,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                calendarDays.forEach { day ->
                    DayCell(day = day, onClick = { onDaySelected(day.dayOfMonth) })
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: HomeCalendarDay,
    onClick: () -> Unit,
) {
    val hasActivity = day.transactionCount > 0
    Surface(
        modifier = Modifier
            .size(42.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = when {
            day.isSelected -> CapPrimaryBlue
            hasActivity -> CapSurfaceHigh.copy(alpha = 0.75f)
            else -> Color.Transparent
        },
        border = BorderStroke(1.dp, if (hasActivity || day.isSelected) CapDivider else CapDivider.copy(alpha = 0.35f)),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(day.dayOfMonth.toString(), fontWeight = if (day.isSelected) FontWeight.Bold else FontWeight.Normal)
            if (hasActivity) {
                Text(
                    text = day.expense.minorUnits.takeIf { it > 0 }?.let { "${it / 1000}k" } ?: "•",
                    style = MaterialTheme.typography.labelSmall,
                    color = CapExpenseCoral,
                )
            }
        }
    }
}

@Composable
private fun DaySummary(
    selectedDay: Int,
    expense: String,
    income: String,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Ngày $selectedDay", style = MaterialTheme.typography.titleLarge)
        Text("Chi $expense · Thu $income", color = CapTextSecondary)
    }
}

@Composable
private fun EmptyDay(onOpenCapture: () -> Unit) {
    CapCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Hôm nay chưa có giao dịch", style = MaterialTheme.typography.titleLarge)
            Text("Thêm một khoản chi hoặc thu để lịch tháng bắt đầu có nhịp.", color = CapTextSecondary)
            TextButton(onClick = onOpenCapture, modifier = Modifier.align(Alignment.End)) {
                Text("Thêm ngay")
            }
        }
    }
}

@Composable
private fun TransactionCard(
    transaction: Transaction,
    wallet: Wallet?,
    category: Category?,
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val sign = if (isExpense) "-" else "+"
    val color = if (isExpense) CapExpenseCoral else CapIncomeMint
    CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = color, shape = CircleShape) {
                Text(
                    text = if (isExpense) "↗" else "↙",
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(category?.name ?: "Khác", style = MaterialTheme.typography.titleMedium)
                Text(
                    listOfNotNull(wallet?.name, transaction.homeDateLabel(), transaction.note).joinToString(" · "),
                    color = CapTextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text("$sign${transaction.amount.formatVnd()}", color = color, style = MaterialTheme.typography.titleMedium)
        }
    }
}
