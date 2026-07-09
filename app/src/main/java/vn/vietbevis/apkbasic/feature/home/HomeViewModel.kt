package vn.vietbevis.apkbasic.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.vietbevis.apkbasic.core.common.MonthRange
import vn.vietbevis.apkbasic.core.common.MonthRanges
import vn.vietbevis.apkbasic.core.common.userMessage
import vn.vietbevis.apkbasic.domain.model.Category
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.Transaction
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.model.Wallet
import vn.vietbevis.apkbasic.domain.reporting.BudgetInsights
import vn.vietbevis.apkbasic.domain.reporting.BudgetInsightsCalculator
import vn.vietbevis.apkbasic.domain.repository.BudgetRepository
import vn.vietbevis.apkbasic.domain.repository.CategoryRepository
import vn.vietbevis.apkbasic.domain.repository.TransactionRepository
import vn.vietbevis.apkbasic.domain.repository.WalletRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class HomeUiState(
    val isLoading: Boolean = true,
    val monthOffset: Int = 0,
    val monthRange: MonthRange = MonthRanges.currentMonth(),
    val selectedDayOfMonth: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val wallets: List<Wallet> = emptyList(),
    val categories: List<Category> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val calendarDays: List<HomeCalendarDay?> = emptyList(), // Nulls for padding
    val dayTransactions: List<Transaction> = emptyList(),
    val monthExpense: Money = Money.vnd(0),
    val monthIncome: Money = Money.vnd(0),
    val dayExpense: Money = Money.vnd(0),
    val dayIncome: Money = Money.vnd(0),
    val budgetInsights: BudgetInsights? = null,
    val errorMessage: String? = null,
)

data class HomeCalendarDay(
    val dayOfMonth: Int,
    val isSelected: Boolean,
    val income: Money,
    val expense: Money,
    val transactionCount: Int,
)

class HomeViewModel(
    private val userProfile: UserProfile,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun navigateMonth(delta: Int) {
        val newOffset = _uiState.value.monthOffset + delta
        _uiState.update { it.copy(monthOffset = newOffset) }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val month = MonthRanges.fromOffset(_uiState.value.monthOffset)
            _uiState.update { it.copy(isLoading = true, monthRange = month, errorMessage = null) }

            val wallets = walletRepository.listWallets(includeArchived = true).getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
                return@launch
            }
            val categories = categoryRepository.listCategories().getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
                return@launch
            }
            val transactions = transactionRepository
                .listTransactions(month.startEpochMillis, month.endEpochMillis)
                .getOrElse { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
                    return@launch
                }

            _uiState.update {
                buildState(
                    base = it.copy(
                        isLoading = false,
                        monthRange = month,
                        wallets = wallets,
                        categories = categories,
                        transactions = transactions,
                    ),
                )
            }
            loadBudgetInsights()
        }
    }

    private suspend fun loadBudgetInsights() {
        val calendar = Calendar.getInstance().apply { timeInMillis = _uiState.value.monthRange.startEpochMillis }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val monthStr = String.format(Locale.US, "%04d-%02d-01", year, month)
        val budgetResult = budgetRepository.getMonthlyBudget(userProfile.id, monthStr)
        
        budgetResult.onSuccess { budget ->
            if (budget != null) {
                _uiState.update { state ->
                    val insights = BudgetInsightsCalculator.calculate(
                        monthlyLimit = budget.amount,
                        totalSpent = state.monthExpense
                    )
                    state.copy(budgetInsights = insights)
                }
            } else {
                _uiState.update { it.copy(budgetInsights = null) }
            }
        }
    }

    fun selectDay(dayOfMonth: Int) {
        _uiState.update { buildState(it.copy(selectedDayOfMonth = dayOfMonth)) }
    }

    private fun buildState(base: HomeUiState): HomeUiState {
        val filteredTransactions = base.transactions
        
        val calendar = Calendar.getInstance().apply { timeInMillis = base.monthRange.startEpochMillis }
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // Adjust selected day if month changes (e.g. 31 -> 28)
        val validatedSelectedDay = base.selectedDayOfMonth.coerceIn(1, daysInMonth)
        
        val dayTransactions = filteredTransactions
            .filter { dayOfMonth(it.occurredAtEpochMillis) == validatedSelectedDay }
            .sortedByDescending { it.occurredAtEpochMillis }

        // Start day of week (1=Sun, 2=Mon...)
        // We want Mon=0, Tue=1 ... Sun=6
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val paddingDays = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
        
        val calendarDays = mutableListOf<HomeCalendarDay?>()
        repeat(paddingDays) { calendarDays.add(null) }
        
        (1..daysInMonth).forEach { day ->
            val dayItems = filteredTransactions.filter { dayOfMonth(it.occurredAtEpochMillis) == day }
            calendarDays.add(
                HomeCalendarDay(
                    dayOfMonth = day,
                    isSelected = day == validatedSelectedDay,
                    income = Money.vnd(dayItems.total(TransactionType.INCOME)),
                    expense = Money.vnd(dayItems.total(TransactionType.EXPENSE)),
                    transactionCount = dayItems.size,
                )
            )
        }

        return base.copy(
            selectedDayOfMonth = validatedSelectedDay,
            calendarDays = calendarDays,
            dayTransactions = dayTransactions,
            monthIncome = Money.vnd(filteredTransactions.total(TransactionType.INCOME)),
            monthExpense = Money.vnd(filteredTransactions.total(TransactionType.EXPENSE)),
            dayIncome = Money.vnd(dayTransactions.total(TransactionType.INCOME)),
            dayExpense = Money.vnd(dayTransactions.total(TransactionType.EXPENSE)),
        )
    }

    private fun List<Transaction>.total(type: TransactionType): Long =
        filter { it.type == type }.sumOf { it.amount.minorUnits }

    private fun dayOfMonth(epochMillis: Long): Int =
        Calendar.getInstance().apply { timeInMillis = epochMillis }.get(Calendar.DAY_OF_MONTH)

    private fun daysInCurrentMonth(): Int =
        Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
}

fun Transaction.homeDateLabel(): String =
    SimpleDateFormat("HH:mm", Locale.forLanguageTag("vi-VN")).format(java.util.Date(occurredAtEpochMillis))
