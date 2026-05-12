package vn.vietbevis.apkbasic.feature.statistics

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
import vn.vietbevis.apkbasic.domain.reporting.FinanceSummary
import vn.vietbevis.apkbasic.domain.reporting.FinanceSummaryCalculator
import vn.vietbevis.apkbasic.domain.repository.CategoryRepository
import vn.vietbevis.apkbasic.domain.repository.TransactionRepository
import vn.vietbevis.apkbasic.domain.repository.WalletRepository

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val monthRange: MonthRange = MonthRanges.currentMonth(),
    val summary: FinanceSummary? = null,
    val errorMessage: String? = null,
)

class StatisticsViewModel(
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

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
            val transactions = transactionRepository.listTransactions(month.startEpochMillis, month.endEpochMillis).getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    summary = FinanceSummaryCalculator.summarize(transactions, wallets, categories, recentLimit = 10),
                )
            }
        }
    }
}
