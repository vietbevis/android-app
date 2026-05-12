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
import vn.vietbevis.apkbasic.domain.model.Wallet
import vn.vietbevis.apkbasic.domain.repository.CategoryRepository
import vn.vietbevis.apkbasic.domain.repository.TransactionRepository
import vn.vietbevis.apkbasic.domain.repository.WalletRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class HomeUiState(
    val isLoading: Boolean = true,
    val monthRange: MonthRange = MonthRanges.currentMonth(),
    val selectedDayOfMonth: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val selectedWalletId: String? = null,
    val wallets: List<Wallet> = emptyList(),
    val categories: List<Category> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val calendarDays: List<HomeCalendarDay> = emptyList(),
    val dayTransactions: List<Transaction> = emptyList(),
    val monthExpense: Money = Money.vnd(0),
    val monthIncome: Money = Money.vnd(0),
    val dayExpense: Money = Money.vnd(0),
    val dayIncome: Money = Money.vnd(0),
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
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val month = MonthRanges.currentMonth()
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
        }
    }

    fun selectDay(dayOfMonth: Int) {
        _uiState.update { buildState(it.copy(selectedDayOfMonth = dayOfMonth)) }
    }

    fun selectWallet(walletId: String?) {
        _uiState.update { buildState(it.copy(selectedWalletId = walletId)) }
    }

    private fun buildState(base: HomeUiState): HomeUiState {
        val filteredTransactions = base.transactions
            .filter { base.selectedWalletId == null || it.walletId == base.selectedWalletId }
        val dayTransactions = filteredTransactions
            .filter { dayOfMonth(it.occurredAtEpochMillis) == base.selectedDayOfMonth }
            .sortedByDescending { it.occurredAtEpochMillis }
        val daysInMonth = daysInCurrentMonth()
        val calendarDays = (1..daysInMonth).map { day ->
            val dayItems = filteredTransactions.filter { dayOfMonth(it.occurredAtEpochMillis) == day }
            HomeCalendarDay(
                dayOfMonth = day,
                isSelected = day == base.selectedDayOfMonth,
                income = Money.vnd(dayItems.total(TransactionType.INCOME)),
                expense = Money.vnd(dayItems.total(TransactionType.EXPENSE)),
                transactionCount = dayItems.size,
            )
        }

        return base.copy(
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
