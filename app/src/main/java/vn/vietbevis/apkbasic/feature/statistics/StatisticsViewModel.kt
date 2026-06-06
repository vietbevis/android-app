package vn.vietbevis.apkbasic.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import vn.vietbevis.apkbasic.domain.repository.CategoryRepository
import vn.vietbevis.apkbasic.domain.repository.TransactionRepository
import vn.vietbevis.apkbasic.domain.repository.WalletRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class StatisticsRangeType {
    MONTH, YEAR, ALL
}

enum class ExportType {
    INCOME, EXPENSE, ALL
}

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val rangeType: StatisticsRangeType = StatisticsRangeType.MONTH,
    val rangeLabel: String = "",
    val summary: FinanceSummary? = null,
    val allTransactions: List<Transaction> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val categories: List<Category> = emptyList(),
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
        loadData(StatisticsRangeType.MONTH)
    }

    fun setRangeType(type: StatisticsRangeType) {
        loadData(type)
    }

    fun refresh() {
        loadData(_uiState.value.rangeType)
    }

    private fun loadData(type: StatisticsRangeType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, rangeType = type, errorMessage = null) }
            
            val start = Calendar.getInstance()
            val end = Calendar.getInstance()
            var label = ""

            when (type) {
                StatisticsRangeType.MONTH -> {
                    start.set(Calendar.DAY_OF_MONTH, 1)
                    start.set(Calendar.HOUR_OF_DAY, 0)
                    start.set(Calendar.MINUTE, 0)
                    start.set(Calendar.SECOND, 0)
                    start.set(Calendar.MILLISECOND, 0)
                    
                    end.timeInMillis = start.timeInMillis
                    end.add(Calendar.MONTH, 1)
                    
                    label = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(start.time)
                }
                StatisticsRangeType.YEAR -> {
                    start.set(Calendar.MONTH, Calendar.JANUARY)
                    start.set(Calendar.DAY_OF_MONTH, 1)
                    start.set(Calendar.HOUR_OF_DAY, 0)
                    start.set(Calendar.MINUTE, 0)
                    start.set(Calendar.SECOND, 0)
                    start.set(Calendar.MILLISECOND, 0)
                    
                    end.timeInMillis = start.timeInMillis
                    end.add(Calendar.YEAR, 1)
                    
                    label = SimpleDateFormat("yyyy", Locale.getDefault()).format(start.time)
                }
                StatisticsRangeType.ALL -> {
                    start.timeInMillis = 0
                    end.timeInMillis = Long.MAX_VALUE
                    label = "Tất cả"
                }
            }

            val wallets = walletRepository.listWallets(includeArchived = true).getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
                return@launch
            }
            val categories = categoryRepository.listCategories().getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
                return@launch
            }
            val transactions = transactionRepository.listTransactions(start.timeInMillis, end.timeInMillis).getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    rangeLabel = label,
                    summary = FinanceSummaryCalculator.summarize(transactions, wallets, categories, recentLimit = 50),
                    allTransactions = transactions,
                    wallets = wallets,
                    categories = categories
                )
            }
        }
    }

    fun exportCsv(exportType: ExportType): String? {
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
        sb.append("Ngày,Loại,Số tiền,Danh mục,Ví,Ghi chú\n")
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        
        // Create lookup maps from the state
        val categoryNameMap = state.categories.associate { it.id to it.name }
        val walletNameMap = state.wallets.associate { it.id to it.name }

        var totalMinorUnits = 0L

        filteredTransactions.forEach { t ->
            val date = dateFormat.format(if (t.updatedAtEpochMillis > 0) t.updatedAtEpochMillis else t.occurredAtEpochMillis)
            val type = if (t.type == TransactionType.INCOME) "Thu nhập" else "Chi tiêu"
            val amountStr = t.amount.formatVnd().replace(",", "")
            
            // Map ID to Name
            val categoryName = categoryNameMap[t.categoryId] ?: "Khác"
            val walletName = walletNameMap[t.walletId] ?: "N/A"
            val note = t.note?.replace("\"", "\"\"") ?: "" // Escape quotes for CSV
            
            sb.append("$date,$type,\"$amountStr\",$categoryName,$walletName,\"$note\"\n")

            // Calculate total for footer
            totalMinorUnits += if (t.type == TransactionType.INCOME) t.amount.minorUnits else -t.amount.minorUnits
        }

        // Add total row for ALL or even specific ones if requested
        val totalMoney = Money.vnd(totalMinorUnits)
        val totalLabel = when (exportType) {
            ExportType.INCOME -> "Tổng thu nhập"
            ExportType.EXPENSE -> "Tổng chi tiêu"
            ExportType.ALL -> "Số dư ròng"
        }
        sb.append("\n,,, $totalLabel,\"${totalMoney.formatVnd().replace(",", "")}\",")

        return sb.toString()
    }
}
