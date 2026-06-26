package vn.vietbevis.apkbasic.feature.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.vietbevis.apkbasic.R
import vn.vietbevis.apkbasic.core.di.AppContainer
import vn.vietbevis.apkbasic.domain.model.Category
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.Transaction
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.model.Wallet
import vn.vietbevis.apkbasic.domain.reporting.BudgetInsights
import vn.vietbevis.apkbasic.ui.components.SnapAvatar
import vn.vietbevis.apkbasic.ui.components.SnapCard
import vn.vietbevis.apkbasic.ui.components.SnapColoredBanner
import vn.vietbevis.apkbasic.ui.components.SnapIconButton
import vn.vietbevis.apkbasic.ui.components.SnapIconTile
import vn.vietbevis.apkbasic.ui.components.SnapListItem
import vn.vietbevis.apkbasic.ui.components.SnapMessageCard
import vn.vietbevis.apkbasic.ui.components.SnapPrimaryButton
import vn.vietbevis.apkbasic.ui.components.SnapSectionHeader
import vn.vietbevis.apkbasic.ui.components.SnapSecondaryPill
import vn.vietbevis.apkbasic.ui.components.SnapSummaryBanner
import vn.vietbevis.apkbasic.ui.components.SnapTopBar
import vn.vietbevis.apkbasic.ui.theme.APKBasicTheme
import vn.vietbevis.apkbasic.ui.theme.SnapBlue
import vn.vietbevis.apkbasic.ui.theme.SnapCoral
import vn.vietbevis.apkbasic.ui.theme.SnapCream
import vn.vietbevis.apkbasic.ui.theme.SnapMint
import vn.vietbevis.apkbasic.ui.theme.SnapNavy
import vn.vietbevis.apkbasic.ui.theme.SnapSlate
import vn.vietbevis.apkbasic.ui.theme.SnapSoftYellow
import vn.vietbevis.apkbasic.ui.theme.SnapWhite
import vn.vietbevis.apkbasic.ui.theme.SnapYellow

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    appContainer: AppContainer,
    userProfile: UserProfile,
    onOpenCapture: () -> Unit,
    onOpenProfile: () -> Unit = {},
    onEditTransaction: (Transaction) -> Unit = {},
    onOpenBudgetDetail: () -> Unit = {},
) {
    val viewModel = remember(userProfile.id) {
        HomeViewModel(
            userProfile = userProfile,
            walletRepository = appContainer.walletRepository,
            categoryRepository = appContainer.categoryRepository,
            transactionRepository = appContainer.transactionRepository,
            budgetRepository = appContainer.budgetRepository,
        )
    }
    val uiState by viewModel.uiState.collectAsState()

    HomeContent(
        modifier = modifier,
        userProfile = userProfile,
        monthLabel = uiState.monthRange.label,
        monthExpense = uiState.monthExpense.formatVnd(),
        monthIncome = uiState.monthIncome.formatVnd(),
        dayExpense = uiState.dayExpense.formatVnd(),
        dayIncome = uiState.dayIncome.formatVnd(),
        budgetInsights = uiState.budgetInsights,
        selectedDay = uiState.selectedDayOfMonth,
        categories = uiState.categories,
        transactions = uiState.dayTransactions,
        calendarDays = uiState.calendarDays,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onRefresh = viewModel::refresh,
        onNavigateMonth = viewModel::navigateMonth,
        onOpenCapture = onOpenCapture,
        onOpenProfile = onOpenProfile,
        onEditTransaction = onEditTransaction,
        onOpenBudgetDetail = onOpenBudgetDetail,
        onDaySelected = viewModel::selectDay,
    )
}

@Composable
private fun HomeContent(
    userProfile: UserProfile,
    monthLabel: String,
    monthExpense: String,
    monthIncome: String,
    dayExpense: String,
    dayIncome: String,
    budgetInsights: BudgetInsights?,
    selectedDay: Int,
    categories: List<Category>,
    transactions: List<Transaction>,
    calendarDays: List<HomeCalendarDay?>,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onNavigateMonth: (Int) -> Unit,
    onOpenCapture: () -> Unit,
    onOpenProfile: () -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onDaySelected: (Int) -> Unit,
    onOpenBudgetDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val userName = userProfile.displayName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.app_name)
    
    val monthOnly = monthLabel.substringBefore("/")
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SnapCream)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            SnapTopBar(
                title = userName,
                modifier = Modifier.padding(top = 12.dp),
                navigationIcon = {
                    SnapAvatar(
                        displayName = userProfile.displayName,
                        email = userProfile.email,
                        avatarUrl = userProfile.avatar,
                        size = 42.dp,
                        updatedAt = userProfile.updatedAt,
                        modifier = Modifier.clickable { onOpenProfile() }
                    )
                },
                actionIcon = {
                    SnapIconButton(
                        iconRes = R.drawable.ic_plus,
                        contentDescription = stringResource(R.string.action_add_transaction),
                        onClick = onOpenCapture,
                    )
                },
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SnapSummaryBanner(
                    label = "Thu tháng này",
                    amount = monthIncome,
                    containerColor = SnapMint,
                    modifier = Modifier.weight(1f)
                )
                SnapSummaryBanner(
                    label = "Chi tháng này",
                    amount = monthExpense,
                    containerColor = SnapCoral,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        budgetInsights?.let { insights ->
            item {
                BudgetInsightsSection(insights = insights, onClick = onOpenBudgetDetail)
            }
        }
        item {
            SnapCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = SnapWhite,
                borderColor = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ngày $selectedDay/$monthOnly:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SnapNavy
                    )
                    Text(
                        text = "Thu $dayIncome",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SnapMint,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Chi $dayExpense",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SnapCoral,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        item {
            MonthStrip(
                monthLabel = monthLabel,
                calendarDays = calendarDays,
                onDaySelected = onDaySelected,
                onNavigateMonth = onNavigateMonth
            )
        }
        item {
            SnapSectionHeader(title = "Giao dịch ngày $selectedDay/$monthOnly", actionText = "Quét biên lai", onAction = onOpenCapture)
        }
        when {
            isLoading -> item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SnapCoral)
                }
            }
            errorMessage != null -> item {
                SnapMessageCard(title = "Không tải được dữ liệu", body = errorMessage, actionText = "Thử lại", onAction = onRefresh)
            }
            transactions.isEmpty() -> item {
                SnapMessageCard(
                    title = "Chưa có giao dịch",
                    body = "Quét biên lai hoặc thêm khoản thu/chi để ngày này có dữ liệu.",
                    actionText = "Thêm ngay",
                    onAction = onOpenCapture,
                )
            }
            else -> items(transactions, key = { it.id }) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    wallet = null, // Wallet filter removed
                    category = categories.firstOrNull { it.id == transaction.categoryId },
                    onClick = { onEditTransaction(transaction) }
                )
            }
        }
        item { Spacer(Modifier.height(100.dp)) }
    }
}

@Composable
private fun BudgetInsightsSection(insights: BudgetInsights, onClick: () -> Unit) {
    SnapCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        containerColor = if (insights.isExceeded) SnapCoral.copy(alpha = 0.1f) else SnapWhite,
        borderColor = if (insights.isExceeded) SnapCoral else Color.Transparent
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (insights.isExceeded) "🚨 Vượt hạn mức!" else "💡 Gợi ý chi tiêu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (insights.isExceeded) SnapCoral else SnapNavy
                )
                Text(
                    text = "${insights.percentSpent}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = SnapSlate
                )
            }

            if (insights.isExceeded) {
                Text(
                    text = "Bạn đã chi quá hạn mức tháng này ${insights.exceededAmount.formatVnd()}!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SnapCoral
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InsightRow(
                        label = "Gợi ý tuần này:",
                        value = "Tối đa ${insights.suggestedWeekly.formatVnd()}",
                        iconRes = R.drawable.ic_chart
                    )
                    InsightRow(
                        label = "Hạn mức hôm nay:",
                        value = insights.suggestedDaily.formatVnd(),
                        iconRes = R.drawable.ic_budget
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightRow(label: String, value: String, iconRes: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = SnapBlue
        )
        Text(label, style = MaterialTheme.typography.bodySmall, color = SnapSlate, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SnapNavy)
    }
}
@Composable
private fun MiniMetric(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    SnapColoredBanner(modifier = modifier, containerColor = color) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = SnapSlate)
            Text(value, style = MaterialTheme.typography.titleMedium, color = SnapNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MonthStrip(
    monthLabel: String,
    calendarDays: List<HomeCalendarDay?>,
    onDaySelected: (Int) -> Unit,
    onNavigateMonth: (Int) -> Unit,
) {
    SnapCard(modifier = Modifier.fillMaxWidth(), containerColor = SnapSoftYellow, borderColor = Color.Transparent) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigateMonth(-1) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Tháng trước",
                        tint = SnapNavy
                    )
                }
                Text("Tháng $monthLabel", style = MaterialTheme.typography.titleLarge, color = SnapNavy)
                IconButton(onClick = { onNavigateMonth(1) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Tháng sau",
                        tint = SnapNavy
                    )
                }
            }

            // Weekday headers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        color = SnapSlate,
                        modifier = Modifier.width(42.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 7
            ) {
                calendarDays.forEach { day ->
                    if (day != null) {
                        DayPill(
                            day = day,
                            onClick = { onDaySelected(day.dayOfMonth) }
                        )
                    } else {
                        // Spacer for padding
                        Spacer(modifier = Modifier.size(width = 42.dp, height = 58.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayPill(day: HomeCalendarDay, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(width = 42.dp, height = 58.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (day.isSelected) SnapCoral else SnapCream,
        contentColor = if (day.isSelected) SnapWhite else SnapNavy,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(day.dayOfMonth.toString(), style = MaterialTheme.typography.labelLarge)
            Text(
                text = if (day.transactionCount > 0) "${day.transactionCount}" else "•",
                style = MaterialTheme.typography.labelSmall,
                color = if (day.isSelected) SnapWhite else SnapSlate,
            )
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    wallet: Wallet?,
    category: Category?,
    onClick: () -> Unit,
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val sign = if (isExpense) "-" else "+"
    
    // Construct the public URL for the transaction photo if it exists
    val supabaseUrl = vn.vietbevis.apkbasic.BuildConfig.SUPABASE_URL.removeSuffix("/")
    val imageUrl = transaction.photoPath?.let { path ->
        // Handle cases where path might already be a URL or has leading slash
        if (path.startsWith("http")) path else "$supabaseUrl/storage/v1/object/public/transaction-photos/${path.removePrefix("/")}"
    }

    SnapListItem(
        title = category?.name ?: if (isExpense) "Khoản chi" else "Khoản thu",
        subtitle = listOfNotNull(wallet?.name, transaction.homeDateLabel(), transaction.note).joinToString(" · "),
        trailingTitle = "$sign${transaction.amount.formatVnd()}",
        trailingSubtitle = if (isExpense) "Chi tiêu" else "Thu nhập",
        iconText = category?.name ?: if (isExpense) "C" else "T",
        imageUrl = imageUrl,
        iconContainerColor = if (isExpense) SnapYellow else SnapMint,
        onClick = onClick,
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
    APKBasicTheme {
        HomeContent(
            userProfile = UserProfile(id = "1", email = "test@example.com", displayName = "SnapChi"),
            monthLabel = "05/26",
            monthExpense = "2.450.000 đ",
            monthIncome = "8.000.000 đ",
            dayExpense = "120.000 đ",
            dayIncome = "0 đ",
            selectedDay = 15,
            categories = emptyList(),
            transactions = emptyList(),
            calendarDays = (1..7).map { HomeCalendarDay(it, it == 3, Money.vnd(0), Money.vnd(0), 0) },
            budgetInsights = null,
            isLoading = false,
            errorMessage = null,
            onRefresh = {},
            onNavigateMonth = {},
            onOpenCapture = {},
            onOpenProfile = {},
            onEditTransaction = {},
            onDaySelected = {},
            onOpenBudgetDetail = {},
        )
    }
}
