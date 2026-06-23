package vn.vietbevis.apkbasic.feature.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.vietbevis.apkbasic.core.common.userMessage
import vn.vietbevis.apkbasic.domain.model.Category
import vn.vietbevis.apkbasic.domain.model.CategoryBudget
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.MonthlyBudget
import vn.vietbevis.apkbasic.domain.model.Transaction
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.repository.BudgetRepository
import vn.vietbevis.apkbasic.domain.repository.CategoryRepository
import vn.vietbevis.apkbasic.domain.repository.TransactionRepository
import java.util.Calendar
import java.util.Locale
import java.util.UUID

data class BudgetsUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val selectedTab: Int = 0, // 0: Detail, 1: Setup
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val monthsWithBudget: Set<String> = emptySet(),
    val currentMonthlyBudget: MonthlyBudget? = null,
    val categories: List<Category> = emptyList(),
    val totalAmountInput: String = "",
    val categoryAmountInputs: Map<String, String> = emptyMap(),
    val isShowingCategoryPicker: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    // Detail data
    val transactions: List<Transaction> = emptyList(),
    val categoryProgressItems: List<CategoryProgressItem> = emptyList(),
    val totalSpent: Money = Money.vnd(0),
    val remainingAmount: Money = Money.vnd(0),
    val safeSpendToday: Money? = null,
    val safeSpendThisWeek: Money? = null,
    val isTotalExceeded: Boolean = false,
    val selectedCategoryId: String? = null,
    val selectedCategoryTransactions: List<Transaction> = emptyList(),
    // Others (spent not in specific category budgets)
    val otherSpent: Money = Money.vnd(0),
    val otherBudgetLimit: Money = Money.vnd(0),
    val otherPercentSpent: Int = 0,
    val isOtherExceeded: Boolean = false,
) {
    val totalCategoryAmount: Long
        get() = categoryAmountInputs.values.sumOf { it.toLongOrNull() ?: 0L }
    
    val isTotalExceededInSetup: Boolean
        get() = (totalAmountInput.toLongOrNull() ?: 0L) < totalCategoryAmount

    val availableCategoriesForPicker: List<Category>
        get() = categories.filter { it.id !in categoryAmountInputs.keys }
}

data class CategoryProgressItem(
    val category: Category,
    val budget: CategoryBudget?,
    val spent: Money,
    val percentSpent: Int,
    val isExceeded: Boolean,
)

class BudgetsViewModel(
    private val userProfile: UserProfile,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BudgetsUiState())
    val uiState: StateFlow<BudgetsUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val categoriesResult = categoryRepository.listCategories()
            val categories = categoriesResult.getOrDefault(emptyList())
                .filter { it.transactionType == TransactionType.EXPENSE }
            
            _uiState.update { it.copy(categories = categories) }
            loadMonthsWithBudget()
            loadSelectedMonthData()
        }
    }

    private suspend fun loadMonthsWithBudget() {
        budgetRepository.listMonthlyBudgets(userProfile.id).onSuccess { list ->
            val months = list.map { it.budgetMonth.substring(0, 7) }.toSet()
            _uiState.update { it.copy(monthsWithBudget = months) }
        }
    }

    fun onMonthYearSelected(month: Int, year: Int) {
        _uiState.update { it.copy(selectedMonth = month, selectedYear = year, infoMessage = null, errorMessage = null) }
        viewModelScope.launch {
            loadSelectedMonthData()
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    private suspend fun loadSelectedMonthData() {
        val month = _uiState.value.selectedMonth
        val year = _uiState.value.selectedYear
        val monthStr = String.format(Locale.US, "%04d-%02d-01", year, month + 1)
        
        _uiState.update { it.copy(isLoading = true) }
        
        // Load Budget
        val budget = budgetRepository.getMonthlyBudget(userProfile.id, monthStr).getOrNull()
        
        // Load Transactions for Detail
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startMillis = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        val endMillis = calendar.timeInMillis
        
        val transactions = transactionRepository.listTransactions(startMillis, endMillis)
            .getOrDefault(emptyList())
            .filter { it.type == TransactionType.EXPENSE }

        // Calculate Progress
        val totalSpentMinor = transactions.sumOf { it.amount.minorUnits }
        val totalLimitMinor = budget?.amount?.minorUnits ?: 0L
        val remainingMinor = totalLimitMinor - totalSpentMinor
        
        val today = Calendar.getInstance()
        val isCurrentMonth = month == today.get(Calendar.MONTH) && year == today.get(Calendar.YEAR)

        val safeToday: Money?
        val safeWeek: Money?

        if (isCurrentMonth && totalLimitMinor > 0) {
            val daysInMonth = today.getActualMaximum(Calendar.DAY_OF_MONTH)
            val dayOfMonth = today.get(Calendar.DAY_OF_MONTH)
            val daysRemaining = (daysInMonth - dayOfMonth + 1).coerceAtLeast(1)

            val remainingLong = remainingMinor.coerceAtLeast(0L)
            val dailyBudget = remainingLong / daysRemaining

            val dayOfWeek = today.get(Calendar.DAY_OF_WEEK)
            val daysLeftInWeek = if (dayOfWeek == Calendar.SUNDAY) 1 else 7 - (dayOfWeek - 2)
            val daysToAccountFor = minOf(daysLeftInWeek, daysRemaining)

            safeToday = Money.vnd(dailyBudget)
            safeWeek = Money.vnd(dailyBudget * daysToAccountFor)
        } else {
            safeToday = null
            safeWeek = null
        }

        val budgetedCategoryIds = budget?.categoryBudgets?.map { it.categoryId }?.toSet() ?: emptySet()

        val progressItems = _uiState.value.categories
            .filter { it.id in budgetedCategoryIds }
            .map { category ->
                val catBudget = budget?.categoryBudgets?.find { it.categoryId == category.id }
                val spent = transactions.filter { it.categoryId == category.id }.sumOf { it.amount.minorUnits }
                val limit = catBudget?.amount?.minorUnits ?: 0L
                val percent = if (limit > 0) ((spent.toDouble() / limit.toDouble()) * 100).toInt() else 0

                CategoryProgressItem(
                    category = category,
                    budget = catBudget,
                    spent = Money.vnd(spent),
                    percentSpent = percent,
                    isExceeded = limit > 0 && spent > limit
                )
            }.sortedWith(compareByDescending<CategoryProgressItem> { it.isExceeded }.thenByDescending { it.percentSpent })

        // Calculate "Others" (spent not in specifically budgeted categories)
        val otherSpentMinor = transactions
            .filter { it.categoryId !in budgetedCategoryIds }
            .sumOf { it.amount.minorUnits }
        
        val totalCategoryLimitMinor = budget?.categoryBudgets?.sumOf { it.amount.minorUnits } ?: 0L
        val otherLimitMinor = (totalLimitMinor - totalCategoryLimitMinor).coerceAtLeast(0L)
        val otherPercent = if (otherLimitMinor > 0) ((otherSpentMinor.toDouble() / otherLimitMinor.toDouble()) * 100).toInt() else 0

        _uiState.update { state ->
            state.copy(
                isLoading = false,
                currentMonthlyBudget = budget,
                totalAmountInput = budget?.amount?.minorUnits?.toString() ?: "",
                categoryAmountInputs = budget?.categoryBudgets?.associate { it.categoryId to it.amount.minorUnits.toString() } ?: emptyMap(),
                transactions = transactions,
                categoryProgressItems = progressItems,
                totalSpent = Money.vnd(totalSpentMinor),
                remainingAmount = Money.vnd(remainingMinor),
                safeSpendToday = safeToday,
                safeSpendThisWeek = safeWeek,
                isTotalExceeded = totalLimitMinor in 1..<totalSpentMinor,
                otherSpent = Money.vnd(otherSpentMinor),
                otherBudgetLimit = Money.vnd(otherLimitMinor),
                otherPercentSpent = otherPercent,
                isOtherExceeded = otherLimitMinor in 1..<otherSpentMinor
            )
        }
    }

    fun onTotalAmountChange(value: String) {
        _uiState.update { it.copy(totalAmountInput = value.filter(Char::isDigit), errorMessage = null) }
    }

    fun onCategoryAmountChange(categoryId: String, value: String) {
        _uiState.update { state ->
            val newInputs = state.categoryAmountInputs.toMutableMap()
            newInputs[categoryId] = value.filter(Char::isDigit)
            state.copy(categoryAmountInputs = newInputs, errorMessage = null)
        }
    }

    fun removeCategoryBudget(categoryId: String) {
        _uiState.update { state ->
            val newInputs = state.categoryAmountInputs.toMutableMap()
            newInputs.remove(categoryId)
            state.copy(categoryAmountInputs = newInputs)
        }
    }

    fun showCategoryPicker(show: Boolean) {
        _uiState.update { it.copy(isShowingCategoryPicker = show) }
    }

    fun addCategoryToBudget(categoryId: String) {
        _uiState.update { state ->
            val newInputs = state.categoryAmountInputs.toMutableMap()
            if (!newInputs.containsKey(categoryId)) {
                newInputs[categoryId] = ""
            }
            state.copy(categoryAmountInputs = newInputs, isShowingCategoryPicker = false)
        }
    }

    fun selectCategory(categoryId: String?) {
        _uiState.update { state ->
            val budgetedCategoryIds = state.currentMonthlyBudget?.categoryBudgets?.map { it.categoryId }?.toSet() ?: emptySet()
            val filtered = when {
                categoryId == null -> emptyList()
                categoryId == "OTHER" -> state.transactions.filter { it.categoryId !in budgetedCategoryIds }
                else -> state.transactions.filter { it.categoryId == categoryId }
            }
            state.copy(selectedCategoryId = categoryId, selectedCategoryTransactions = filtered)
        }
    }

    fun saveBudget() {
        val state = _uiState.value
        val totalAmount = state.totalAmountInput.toLongOrNull() ?: 0L
        if (totalAmount <= 0) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập hạn mức tổng lớn hơn 0") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            
            val monthStr = String.format(Locale.US, "%04d-%02d-01", state.selectedYear, state.selectedMonth + 1)
            val budgetId = state.currentMonthlyBudget?.id ?: UUID.randomUUID().toString()
            
            val monthlyBudget = MonthlyBudget(
                id = budgetId,
                userId = userProfile.id,
                amount = Money.vnd(totalAmount),
                budgetMonth = monthStr
            )

            budgetRepository.upsertMonthlyBudget(monthlyBudget).onSuccess { savedBudget ->
                // Delete categories that were removed in UI
                val originalCategoryIds = state.currentMonthlyBudget?.categoryBudgets?.map { it.categoryId } ?: emptyList()
                val currentCategoryIds = state.categoryAmountInputs.keys
                val removedCategoryIds = originalCategoryIds.filter { it !in currentCategoryIds }

                if (removedCategoryIds.isNotEmpty()) {
                    budgetRepository.deleteCategoryBudgets(savedBudget.id, removedCategoryIds)
                }

                val categoryBudgets = state.categoryAmountInputs.map { (catId, amt) ->
                    CategoryBudget(
                        monthlyBudgetId = savedBudget.id,
                        categoryId = catId,
                        amount = Money.vnd(amt.toLongOrNull() ?: 0L)
                    )
                }
                
                budgetRepository.upsertCategoryBudgets(categoryBudgets).onSuccess {
                    _uiState.update { 
                        it.copy(
                            isSaving = false, 
                            infoMessage = "Đã lưu ngân sách tháng ${state.selectedMonth + 1}/${state.selectedYear}"
                        )
                    }
                    loadMonthsWithBudget()
                    loadSelectedMonthData()
                }.onFailure { error ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = error.userMessage()) }
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false, errorMessage = error.userMessage()) }
            }
        }
    }
}
