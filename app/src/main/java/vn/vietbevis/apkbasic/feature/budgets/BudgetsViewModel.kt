package vn.vietbevis.apkbasic.feature.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.vietbevis.apkbasic.core.common.MonthRanges
import vn.vietbevis.apkbasic.core.common.userMessage
import vn.vietbevis.apkbasic.domain.model.Budget
import vn.vietbevis.apkbasic.domain.model.BudgetCycle
import vn.vietbevis.apkbasic.domain.model.BudgetScope
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.Transaction
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.reporting.BudgetProgress
import vn.vietbevis.apkbasic.domain.reporting.BudgetProgressCalculator
import vn.vietbevis.apkbasic.domain.repository.BudgetRepository
import vn.vietbevis.apkbasic.domain.repository.TransactionRepository
import java.util.Calendar
import java.util.UUID

data class BudgetsUiState(
    val isLoading: Boolean = true,
    val budgets: List<BudgetProgressItem> = emptyList(),
    val nameInput: String = "",
    val amountInput: String = "",
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)

data class BudgetProgressItem(
    val budget: Budget,
    val progress: BudgetProgress,
)

class BudgetsViewModel(
    private val userProfile: UserProfile,
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BudgetsUiState())
    val uiState: StateFlow<BudgetsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val budgets = budgetRepository.listBudgets(includeArchived = false).getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
                return@launch
            }
            val month = MonthRanges.currentMonth()
            val transactions = transactionRepository.listTransactions(month.startEpochMillis, month.endEpochMillis).getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    budgets = budgets.map { budget -> budget.toProgressItem(transactions) },
                )
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(nameInput = value, errorMessage = null) }
    }

    fun onAmountChange(value: String) {
        _uiState.update { it.copy(amountInput = value.filter(Char::isDigit), errorMessage = null) }
    }

    fun createBudget() {
        val state = _uiState.value
        val amount = state.amountInput.toLongOrNull()
        if (state.nameInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nhập tên ngân sách.") }
            return
        }
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(errorMessage = "Nhập số tiền ngân sách lớn hơn 0.") }
            return
        }

        viewModelScope.launch {
            val budget = Budget(
                id = UUID.randomUUID().toString(),
                userId = userProfile.id,
                name = state.nameInput.trim(),
                amount = Money.vnd(amount),
                cycle = BudgetCycle.MONTHLY,
                scope = BudgetScope.Total,
                color = "#1088FF",
                startsAtEpochMillis = MonthRanges.currentMonth().startEpochMillis,
            )
            budgetRepository.createBudget(budget)
                .onSuccess {
                    _uiState.update { current ->
                        current.copy(nameInput = "", amountInput = "", infoMessage = "Đã tạo ngân sách.")
                    }
                    refresh()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.userMessage()) }
                }
        }
    }

    fun archiveBudget(budgetId: String) {
        viewModelScope.launch {
            budgetRepository.archiveBudget(budgetId)
                .onSuccess { refresh() }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.userMessage()) } }
        }
    }

    private fun Budget.toProgressItem(transactions: List<Transaction>): BudgetProgressItem {
        val categoryId = (scope as? BudgetScope.Category)?.categoryId
        val spent = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .filter { categoryId == null || it.categoryId == categoryId }
            .sumOf { it.amount.minorUnits }
        return BudgetProgressItem(
            budget = this,
            progress = BudgetProgressCalculator.calculate(
                budget = amount,
                spent = Money.vnd(spent),
                daysRemaining = daysRemainingInMonth(),
            ),
        )
    }

    private fun daysRemainingInMonth(): Int {
        val calendar = Calendar.getInstance()
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH) + 1
    }
}
