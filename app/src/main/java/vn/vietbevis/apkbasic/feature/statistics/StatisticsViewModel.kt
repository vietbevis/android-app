package vn.vietbevis.apkbasic.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.vietbevis.apkbasic.core.common.userMessage
import vn.vietbevis.apkbasic.domain.model.Category
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.Transaction
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.model.Wallet
import vn.vietbevis.apkbasic.domain.reporting.FinanceSummary
import vn.vietbevis.apkbasic.domain.reporting.FinanceSummaryCalculator
import vn.vietbevis.apkbasic.domain.model.MonthlyBudget
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.repository.BudgetRepository
import vn.vietbevis.apkbasic.domain.repository.CategoryRepository
import vn.vietbevis.apkbasic.domain.repository.TransactionRepository
import vn.vietbevis.apkbasic.domain.repository.WalletRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class StatisticsRangeType {
    MONTH, YEAR
}

enum class ExportType {
    INCOME, EXPENSE, ALL
}

data class MonthlyTrend(
    val label: String,
    val income: Long,
    val expense: Long
)

data class BudgetGoalStatus(
    val monthlyBudget: MonthlyBudget?,
    val totalSpent: Money,
    val isTotalExceeded: Boolean,
    val totalProgress: Float,
    val categoryStatuses: List<CategoryGoalStatus>,
    val otherBudget: Money,
    val otherSpent: Money,
    val otherProgress: Float,
    val isOtherExceeded: Boolean
)

data class CategoryGoalStatus(
    val category: Category,
    val budgetAmount: Money,
    val spentAmount: Money,
    val isExceeded: Boolean,
    val progress: Float
)

data class YearlyBudgetSummary(
    val monthsBudgeted: Int,
    val monthsSuccessful: Int, // Stayed under budget
    val totalBudgeted: Money,
    val totalSpent: Money,
    val performancePercent: Int
)

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val rangeType: StatisticsRangeType = StatisticsRangeType.MONTH,
    val pivotYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val pivotMonth: Int = Calendar.getInstance().get(Calendar.MONTH), // 0-11
    val rangeLabel: String = "",
    val summary: FinanceSummary? = null,
    val allTransactions: List<Transaction> = emptyList(),
    val monthlyTrends: List<MonthlyTrend> = emptyList(),
    val budgetGoalStatus: BudgetGoalStatus? = null,
    val yearlyBudgetSummary: YearlyBudgetSummary? = null,
    val wallets: List<Wallet> = emptyList(),
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null,
)

class StatisticsViewModel(
    private val userProfile: UserProfile,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadData()
    }

    fun setRangeType(type: StatisticsRangeType) {
        _uiState.update { it.copy(rangeType = type, summary = null) }
        loadData()
    }

    fun navigatePrevious() {
        _uiState.update { state ->
            if (state.rangeType == StatisticsRangeType.MONTH) {
                var newMonth = state.pivotMonth - 1
                var newYear = state.pivotYear
                if (newMonth < 0) {
                    newMonth = 11
                    newYear -= 1
                }
                state.copy(pivotMonth = newMonth, pivotYear = newYear, summary = null)
            } else {
                state.copy(pivotYear = state.pivotYear - 1, summary = null)
            }
        }
        loadData()
    }

    fun navigateNext() {
        _uiState.update { state ->
            if (state.rangeType == StatisticsRangeType.MONTH) {
                var newMonth = state.pivotMonth + 1
                var newYear = state.pivotYear
                if (newMonth > 11) {
                    newMonth = 0
                    newYear += 1
                }
                state.copy(pivotMonth = newMonth, pivotYear = newYear, summary = null)
            } else {
                state.copy(pivotYear = state.pivotYear + 1, summary = null)
            }
        }
        loadData()
    }

    fun refresh() {
        loadData()
    }

    private fun loadData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val currentState = _uiState.value
            val type = currentState.rangeType
            val year = currentState.pivotYear
            val month = currentState.pivotMonth
            
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // Calculate UTC range for database query
            val start = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(Calendar.YEAR, year)
                if (type == StatisticsRangeType.MONTH) {
                    set(Calendar.MONTH, month)
                } else {
                    set(Calendar.MONTH, Calendar.JANUARY)
                }
                set(Calendar.DAY_OF_MONTH, 1)
            }
            
            val end = start.clone() as Calendar
            if (type == StatisticsRangeType.MONTH) {
                end.add(Calendar.MONTH, 1)
            } else {
                end.add(Calendar.YEAR, 1)
            }

            val label = if (type == StatisticsRangeType.MONTH) {
                String.format(Locale.US, "%02d/%d", month + 1, year)
            } else {
                year.toString()
            }

            val wallets = walletRepository.listWallets(includeArchived = true).getOrElse { emptyList() }
            val categories = categoryRepository.listCategories().getOrElse { emptyList() }
            
            transactionRepository.listTransactions(start.timeInMillis, end.timeInMillis).onSuccess { transactions ->
                // Calculate monthly trends for YEAR view
                val monthlyTrends = if (type == StatisticsRangeType.YEAR) {
                    (0..11).map { monthIdx ->
                        val mStart = start.clone() as Calendar
                        mStart.set(Calendar.MONTH, monthIdx)
                        val mEnd = mStart.clone() as Calendar
                        mEnd.add(Calendar.MONTH, 1)
                        
                        val monthTxs = transactions.filter { 
                            it.occurredAtEpochMillis >= mStart.timeInMillis && it.occurredAtEpochMillis < mEnd.timeInMillis 
                        }
                        
                        MonthlyTrend(
                            label = (monthIdx + 1).toString(),
                            income = monthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.minorUnits },
                            expense = monthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.minorUnits }
                        )
                    }
                } else {
                    emptyList()
                }

                // --- Calculate Goals ---
                var budgetGoalStatus: BudgetGoalStatus? = null
                var yearlyBudgetSummary: YearlyBudgetSummary? = null

                if (type == StatisticsRangeType.MONTH) {
                    val monthStr = String.format(Locale.US, "%04d-%02d-01", year, month + 1)
                    val budget = budgetRepository.getMonthlyBudget(userProfile.id, monthStr).getOrNull()
                    
                    if (budget != null) {
                        val expenseTxs = transactions.filter { it.type == TransactionType.EXPENSE }
                        val totalSpent = expenseTxs.sumOf { it.amount.minorUnits }
                        
                        val categoryStatuses = budget.categoryBudgets.mapNotNull { cb ->
                            val cat = categories.find { it.id == cb.categoryId } ?: return@mapNotNull null
                            val spent = expenseTxs.filter { it.categoryId == cat.id }.sumOf { it.amount.minorUnits }
                            CategoryGoalStatus(
                                category = cat,
                                budgetAmount = cb.amount,
                                spentAmount = Money.vnd(spent),
                                isExceeded = spent > cb.amount.minorUnits,
                                progress = if (cb.amount.minorUnits > 0) spent.toFloat() / cb.amount.minorUnits else 1f
                            )
                        }

                        // Calculate "Other" budget logic
                        val budgetedCategoryIds = budget.categoryBudgets.map { it.categoryId }.toSet()
                        val totalBudgetedMinor = budget.amount.minorUnits
                        val specificCategoryBudgetTotal = budget.categoryBudgets.sumOf { it.amount.minorUnits }
                        val otherBudgetMinor = (totalBudgetedMinor - specificCategoryBudgetTotal).coerceAtLeast(0L)
                        
                        val otherSpentMinor = expenseTxs
                            .filter { it.categoryId !in budgetedCategoryIds }
                            .sumOf { it.amount.minorUnits }
                        
                        budgetGoalStatus = BudgetGoalStatus(
                            monthlyBudget = budget,
                            totalSpent = Money.vnd(totalSpent),
                            isTotalExceeded = totalSpent > totalBudgetedMinor,
                            totalProgress = if (totalBudgetedMinor > 0) totalSpent.toFloat() / totalBudgetedMinor else 1f,
                            categoryStatuses = categoryStatuses,
                            otherBudget = Money.vnd(otherBudgetMinor),
                            otherSpent = Money.vnd(otherSpentMinor),
                            otherProgress = if (otherBudgetMinor > 0) otherSpentMinor.toFloat() / otherBudgetMinor else 1f,
                            isOtherExceeded = otherBudgetMinor in 1..<otherSpentMinor
                        )
                    }
                } else if (type == StatisticsRangeType.YEAR) {
                    val allBudgets = budgetRepository.listMonthlyBudgets(userProfile.id).getOrDefault(emptyList())
                        .filter { it.budgetMonth.startsWith(year.toString()) }
                    
                    if (allBudgets.isNotEmpty()) {
                        var monthsSuccessful = 0
                        var totalBudgetedMinor = 0L
                        var totalSpentMinor = 0L
                        
                        allBudgets.forEach { b ->
                            val monthParts = b.budgetMonth.split("-")
                            val m = monthParts[1].toInt() - 1
                            val mStart = start.clone() as Calendar
                            mStart.set(Calendar.MONTH, m)
                            val mEnd = mStart.clone() as Calendar
                            mEnd.add(Calendar.MONTH, 1)
                            
                            val monthSpent = transactions
                                .filter { it.type == TransactionType.EXPENSE && it.occurredAtEpochMillis >= mStart.timeInMillis && it.occurredAtEpochMillis < mEnd.timeInMillis }
                                .sumOf { it.amount.minorUnits }
                            
                            if (monthSpent <= b.amount.minorUnits) monthsSuccessful++
                            totalBudgetedMinor += b.amount.minorUnits
                            totalSpentMinor += monthSpent
                        }
                        
                        yearlyBudgetSummary = YearlyBudgetSummary(
                            monthsBudgeted = allBudgets.size,
                            monthsSuccessful = monthsSuccessful,
                            totalBudgeted = Money.vnd(totalBudgetedMinor),
                            totalSpent = Money.vnd(totalSpentMinor),
                            performancePercent = if (allBudgets.isNotEmpty()) (monthsSuccessful * 100) / allBudgets.size else 0
                        )
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        rangeLabel = label,
                        summary = FinanceSummaryCalculator.summarize(transactions, wallets, categories, recentLimit = 50),
                        allTransactions = transactions,
                        monthlyTrends = monthlyTrends,
                        budgetGoalStatus = budgetGoalStatus,
                        yearlyBudgetSummary = yearlyBudgetSummary,
                        wallets = wallets,
                        categories = categories
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
            }
        }
    }

    fun exportCsv(exportType: ExportType, context: android.content.Context): String? {
        val state = _uiState.value
        val allTransactions = state.allTransactions
        if (allTransactions.isEmpty()) return null

        val filteredTransactions = when (exportType) {
            ExportType.INCOME -> allTransactions.filter { it.type == TransactionType.INCOME }
            ExportType.EXPENSE -> allTransactions.filter { it.type == TransactionType.EXPENSE }
            ExportType.ALL -> allTransactions
        }.sortedByDescending { if (it.updatedAtEpochMillis > 0) it.updatedAtEpochMillis else it.occurredAtEpochMillis }

        if (filteredTransactions.isEmpty()) return null

        val sb = StringBuilder()
        // CSV Header
        sb.append(context.getString(vn.vietbevis.apkbasic.R.string.statistics_csv_header))
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        
        // Create lookup maps from the state
        val categoryNameMap = state.categories.associate { it.id to it.name }
        val walletNameMap = state.wallets.associate { it.id to it.name }

        var totalMinorUnits = 0L

        filteredTransactions.forEach { t ->
            val date = dateFormat.format(if (t.updatedAtEpochMillis > 0) t.updatedAtEpochMillis else t.occurredAtEpochMillis)
            val type = if (t.type == TransactionType.INCOME) context.getString(vn.vietbevis.apkbasic.R.string.transaction_income_type) else context.getString(vn.vietbevis.apkbasic.R.string.transaction_expense_type)
            val amountStr = t.amount.formatVnd().replace(",", "")
            
            // Map ID to Name
            val categoryName = categoryNameMap[t.categoryId] ?: context.getString(vn.vietbevis.apkbasic.R.string.wallet_type_other)
            val walletName = walletNameMap[t.walletId] ?: "N/A"
            val note = t.note?.replace("\"", "\"\"") ?: "" // Escape quotes for CSV
            
            sb.append("$date,$type,\"$amountStr\",$categoryName,$walletName,\"$note\"\n")

            // Calculate total for footer
            totalMinorUnits += if (t.type == TransactionType.INCOME) t.amount.minorUnits else -t.amount.minorUnits
        }

        // Add total row for ALL or even specific ones if requested
        val totalMoney = Money.vnd(totalMinorUnits)
        val totalLabel = when (exportType) {
            ExportType.INCOME -> context.getString(vn.vietbevis.apkbasic.R.string.statistics_total_income)
            ExportType.EXPENSE -> context.getString(vn.vietbevis.apkbasic.R.string.statistics_total_expense)
            ExportType.ALL -> context.getString(vn.vietbevis.apkbasic.R.string.statistics_net_balance)
        }
        sb.append("\n,,, $totalLabel,\"${totalMoney.formatVnd().replace(",", "")}\",")

        return sb.toString()
    }
}
