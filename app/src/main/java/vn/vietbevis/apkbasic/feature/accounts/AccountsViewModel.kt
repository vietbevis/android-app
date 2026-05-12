package vn.vietbevis.apkbasic.feature.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.vietbevis.apkbasic.core.common.MonthRanges
import vn.vietbevis.apkbasic.core.common.userMessage
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.model.Wallet
import vn.vietbevis.apkbasic.domain.model.Investment
import vn.vietbevis.apkbasic.domain.model.InvestmentStatus
import vn.vietbevis.apkbasic.domain.model.InvestmentType
import vn.vietbevis.apkbasic.domain.model.Loan
import vn.vietbevis.apkbasic.domain.model.LoanInterest
import vn.vietbevis.apkbasic.domain.model.LoanInterestCalculationMethod
import vn.vietbevis.apkbasic.domain.model.LoanInterestInputMode
import vn.vietbevis.apkbasic.domain.model.LoanRepaymentMethod
import vn.vietbevis.apkbasic.domain.model.LoanStatus
import vn.vietbevis.apkbasic.domain.model.LoanType
import vn.vietbevis.apkbasic.domain.model.Transfer
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.repository.InvestmentRepository
import vn.vietbevis.apkbasic.domain.repository.LoanRepository
import vn.vietbevis.apkbasic.domain.repository.TransactionRepository
import vn.vietbevis.apkbasic.domain.repository.TransferRepository
import vn.vietbevis.apkbasic.domain.repository.WalletRepository
import java.util.UUID

data class AccountsUiState(
    val isLoading: Boolean = true,
    val wallets: List<AccountBalance> = emptyList(),
    val totalBalance: Money = Money.vnd(0),
    val income: Money = Money.vnd(0),
    val expense: Money = Money.vnd(0),
    val transfers: List<Transfer> = emptyList(),
    val loans: List<Loan> = emptyList(),
    val investments: List<Investment> = emptyList(),
    val transferAmountInput: String = "",
    val transferFromWalletId: String? = null,
    val transferToWalletId: String? = null,
    val loanNameInput: String = "",
    val loanPrincipalInput: String = "",
    val investmentNameInput: String = "",
    val investmentPrincipalInput: String = "",
    val infoMessage: String? = null,
    val errorMessage: String? = null,
)

data class AccountBalance(
    val wallet: Wallet,
    val balance: Money,
)

class AccountsViewModel(
    private val userProfile: UserProfile,
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val transferRepository: TransferRepository,
    private val loanRepository: LoanRepository,
    private val investmentRepository: InvestmentRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val wallets = walletRepository.listWallets(includeArchived = false).getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
                return@launch
            }
            val month = MonthRanges.currentMonth()
            val transactions = transactionRepository.listTransactions(month.startEpochMillis, month.endEpochMillis).getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
                return@launch
            }
            val transfers = transferRepository.listTransfers(month.startEpochMillis, month.endEpochMillis).getOrElse { emptyList() }
            val loans = loanRepository.listLoans(includeArchived = false).getOrElse { emptyList() }
            val investments = investmentRepository.listInvestments(includeArchived = false).getOrElse { emptyList() }
            val balances = wallets.map { wallet ->
                val delta = transactions.filter { it.walletId == wallet.id }.sumOf {
                    if (it.type == TransactionType.INCOME) it.amount.minorUnits else -it.amount.minorUnits
                }
                val transferDelta = transfers.sumOf { transfer ->
                    when (wallet.id) {
                        transfer.fromWalletId -> -transfer.amount.minorUnits - transfer.fee.minorUnits
                        transfer.toWalletId -> transfer.amount.minorUnits
                        else -> 0L
                    }
                }
                AccountBalance(wallet, Money.vnd(wallet.initialBalance.minorUnits + delta + transferDelta))
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    wallets = balances,
                    transferFromWalletId = it.transferFromWalletId ?: wallets.getOrNull(0)?.id,
                    transferToWalletId = it.transferToWalletId ?: wallets.getOrNull(1)?.id,
                    totalBalance = Money.vnd(balances.sumOf { balance -> balance.balance.minorUnits }),
                    income = Money.vnd(transactions.filter { tx -> tx.type == TransactionType.INCOME }.sumOf { tx -> tx.amount.minorUnits }),
                    expense = Money.vnd(transactions.filter { tx -> tx.type == TransactionType.EXPENSE }.sumOf { tx -> tx.amount.minorUnits }),
                    transfers = transfers,
                    loans = loans,
                    investments = investments,
                )
            }
        }
    }

    fun onTransferAmountChange(value: String) {
        _uiState.update { it.copy(transferAmountInput = value.filter(Char::isDigit), errorMessage = null) }
    }

    fun onTransferFromWalletSelected(walletId: String) {
        _uiState.update { it.copy(transferFromWalletId = walletId, errorMessage = null) }
    }

    fun onTransferToWalletSelected(walletId: String) {
        _uiState.update { it.copy(transferToWalletId = walletId, errorMessage = null) }
    }

    fun createTransfer() {
        val state = _uiState.value
        val amount = state.transferAmountInput.toLongOrNull()
        val from = state.transferFromWalletId
        val to = state.transferToWalletId
        if (from == null || to == null || from == to) {
            _uiState.update { it.copy(errorMessage = "Chọn hai tài khoản khác nhau.") }
            return
        }
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(errorMessage = "Nhập số tiền chuyển lớn hơn 0.") }
            return
        }
        viewModelScope.launch {
            transferRepository.createTransfer(
                Transfer(
                    id = UUID.randomUUID().toString(),
                    userId = userProfile.id,
                    fromWalletId = from,
                    toWalletId = to,
                    amount = Money.vnd(amount),
                    occurredAtEpochMillis = System.currentTimeMillis(),
                ),
            ).onSuccess {
                _uiState.update { it.copy(transferAmountInput = "", infoMessage = "Đã tạo chuyển tiền.") }
                refresh()
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.userMessage()) }
            }
        }
    }

    fun onLoanNameChange(value: String) {
        _uiState.update { it.copy(loanNameInput = value, errorMessage = null) }
    }

    fun onLoanPrincipalChange(value: String) {
        _uiState.update { it.copy(loanPrincipalInput = value.filter(Char::isDigit), errorMessage = null) }
    }

    fun createLoan() {
        val state = _uiState.value
        val principal = state.loanPrincipalInput.toLongOrNull()
        if (state.loanNameInput.isBlank() || principal == null || principal <= 0) {
            _uiState.update { it.copy(errorMessage = "Nhập tên và số tiền khoản vay.") }
            return
        }
        viewModelScope.launch {
            loanRepository.createLoan(
                Loan(
                    id = UUID.randomUUID().toString(),
                    userId = userProfile.id,
                    type = LoanType.BORROWED,
                    name = state.loanNameInput.trim(),
                    principal = Money.vnd(principal),
                    interest = LoanInterest(
                        inputMode = LoanInterestInputMode.PERCENT_PER_YEAR,
                        calculationMethod = LoanInterestCalculationMethod.SIMPLE,
                        value = Money.vnd(0),
                    ),
                    repaymentMethod = LoanRepaymentMethod.MANUAL,
                    startAtEpochMillis = System.currentTimeMillis(),
                    status = LoanStatus.ACTIVE,
                ),
            ).onSuccess {
                _uiState.update { it.copy(loanNameInput = "", loanPrincipalInput = "", infoMessage = "Đã tạo khoản vay.") }
                refresh()
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.userMessage()) }
            }
        }
    }

    fun archiveLoan(loanId: String) {
        viewModelScope.launch {
            loanRepository.archiveLoan(loanId)
                .onSuccess { refresh() }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.userMessage()) } }
        }
    }

    fun onInvestmentNameChange(value: String) {
        _uiState.update { it.copy(investmentNameInput = value, errorMessage = null) }
    }

    fun onInvestmentPrincipalChange(value: String) {
        _uiState.update { it.copy(investmentPrincipalInput = value.filter(Char::isDigit), errorMessage = null) }
    }

    fun createInvestment() {
        val state = _uiState.value
        val principal = state.investmentPrincipalInput.toLongOrNull()
        if (state.investmentNameInput.isBlank() || principal == null || principal < 0) {
            _uiState.update { it.copy(errorMessage = "Nhập tên và giá trị đầu tư.") }
            return
        }
        viewModelScope.launch {
            investmentRepository.createInvestment(
                Investment(
                    id = UUID.randomUUID().toString(),
                    userId = userProfile.id,
                    name = state.investmentNameInput.trim(),
                    type = InvestmentType.OTHER,
                    principal = Money.vnd(principal),
                    currentValue = Money.vnd(principal),
                    status = InvestmentStatus.ACTIVE,
                ),
            ).onSuccess {
                _uiState.update { it.copy(investmentNameInput = "", investmentPrincipalInput = "", infoMessage = "Đã tạo đầu tư.") }
                refresh()
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.userMessage()) }
            }
        }
    }

    fun archiveInvestment(investmentId: String) {
        viewModelScope.launch {
            investmentRepository.archiveInvestment(investmentId)
                .onSuccess { refresh() }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.userMessage()) } }
        }
    }
}
