package vn.vietbevis.apkbasic.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.vietbevis.apkbasic.core.common.MonthRanges
import vn.vietbevis.apkbasic.core.common.userMessage
import vn.vietbevis.apkbasic.domain.model.AppLanguage
import vn.vietbevis.apkbasic.domain.model.Category
import vn.vietbevis.apkbasic.domain.model.Friend
import vn.vietbevis.apkbasic.domain.model.FriendStatus
import vn.vietbevis.apkbasic.domain.model.Group
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.RecurringFrequency
import vn.vietbevis.apkbasic.domain.model.RecurringSchedule
import vn.vietbevis.apkbasic.domain.model.RecurringTransaction
import vn.vietbevis.apkbasic.domain.model.SharedTransaction
import vn.vietbevis.apkbasic.domain.model.Transaction
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.model.ThemeMode
import vn.vietbevis.apkbasic.domain.model.UserPreference
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.model.Wallet
import vn.vietbevis.apkbasic.domain.model.WeekStart
import vn.vietbevis.apkbasic.domain.repository.CategoryRepository
import vn.vietbevis.apkbasic.domain.repository.RecurringTransactionRepository
import vn.vietbevis.apkbasic.domain.repository.SharingRepository
import vn.vietbevis.apkbasic.domain.repository.TransactionRepository
import vn.vietbevis.apkbasic.domain.repository.UserPreferenceRepository
import vn.vietbevis.apkbasic.domain.repository.WalletRepository
import java.util.UUID

data class ProfileUiState(
    val isLoading: Boolean = true,
    val transactionCount: Int = 0,
    val income: Money = Money.vnd(0),
    val expense: Money = Money.vnd(0),
    val balance: Money = Money.vnd(0),
    val preference: UserPreference? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val recurringTransactions: List<RecurringTransaction> = emptyList(),
    val friends: List<Friend> = emptyList(),
    val groups: List<Group> = emptyList(),
    val sharedTransactions: List<SharedTransaction> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val categories: List<Category> = emptyList(),
    val recurringAmountInput: String = "",
    val recurringNoteInput: String = "",
    val friendUserIdInput: String = "",
    val groupNameInput: String = "",
    val selectedShareGroupId: String? = null,
    val selectedWalletId: String? = null,
    val selectedCategoryId: String? = null,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
)

class ProfileViewModel(
    private val userProfile: UserProfile,
    private val transactionRepository: TransactionRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val userPreferenceRepository: UserPreferenceRepository,
    private val sharingRepository: SharingRepository,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val month = MonthRanges.currentMonth()
            val transactions = transactionRepository.listTransactions(month.startEpochMillis, month.endEpochMillis).getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
                return@launch
            }
            val preference = userPreferenceRepository.readPreferences().getOrElse {
                val defaultPreference = UserPreference(userId = userProfile.id)
                userPreferenceRepository.updatePreferences(defaultPreference).getOrDefault(defaultPreference)
            }
            val recurring = recurringTransactionRepository.listRecurringTransactions(includeArchived = false).getOrElse { emptyList() }
            val friends = sharingRepository.listFriends().getOrElse { emptyList() }
            val groups = sharingRepository.listGroups().getOrElse { emptyList() }
            val sharedTransactions = sharingRepository.listSharedTransactions().getOrElse { emptyList() }
            val wallets = walletRepository.listWallets(includeArchived = false).getOrElse { emptyList() }
            val categories = categoryRepository.listCategories(TransactionType.EXPENSE).getOrElse { emptyList() }
            val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.minorUnits }
            val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.minorUnits }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    transactionCount = transactions.size,
                    income = Money.vnd(income),
                    expense = Money.vnd(expense),
                    balance = Money.vnd(income - expense),
                    preference = preference,
                    recentTransactions = transactions,
                    recurringTransactions = recurring,
                    friends = friends,
                    groups = groups,
                    sharedTransactions = sharedTransactions,
                    wallets = wallets,
                    categories = categories,
                    selectedShareGroupId = it.selectedShareGroupId ?: groups.firstOrNull()?.id,
                    selectedWalletId = it.selectedWalletId ?: wallets.firstOrNull()?.id,
                    selectedCategoryId = it.selectedCategoryId ?: categories.firstOrNull()?.id,
                    errorMessage = null,
                )
            }
        }
    }

    fun onRecurringAmountChange(value: String) {
        _uiState.update { it.copy(recurringAmountInput = value.filter(Char::isDigit), errorMessage = null) }
    }

    fun onRecurringNoteChange(value: String) {
        _uiState.update { it.copy(recurringNoteInput = value, errorMessage = null) }
    }

    fun onFriendUserIdChange(value: String) {
        _uiState.update { it.copy(friendUserIdInput = value.trim(), errorMessage = null) }
    }

    fun onGroupNameChange(value: String) {
        _uiState.update { it.copy(groupNameInput = value, errorMessage = null) }
    }

    fun onShareGroupSelected(groupId: String) {
        _uiState.update { it.copy(selectedShareGroupId = groupId, errorMessage = null) }
    }

    fun onWalletSelected(walletId: String) {
        _uiState.update { it.copy(selectedWalletId = walletId, errorMessage = null) }
    }

    fun onCategorySelected(categoryId: String) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, errorMessage = null) }
    }

    fun setLanguage(language: AppLanguage) {
        updatePreference { it.copy(language = language) }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        updatePreference { it.copy(themeMode = themeMode) }
    }

    fun setWeekStart(weekStart: WeekStart) {
        updatePreference { it.copy(weekStartsOn = weekStart) }
    }

    fun createRecurringTransaction() {
        val state = _uiState.value
        val amount = state.recurringAmountInput.toLongOrNull()
        val walletId = state.selectedWalletId
        if (amount == null || amount <= 0 || walletId == null) {
            _uiState.update { it.copy(errorMessage = "Nhập số tiền và chọn tài khoản cho giao dịch định kỳ.") }
            return
        }
        viewModelScope.launch {
            recurringTransactionRepository.createRecurringTransaction(
                RecurringTransaction(
                    id = UUID.randomUUID().toString(),
                    userId = userProfile.id,
                    walletId = walletId,
                    categoryId = state.selectedCategoryId,
                    type = TransactionType.EXPENSE,
                    amount = Money.vnd(amount),
                    schedule = RecurringSchedule(
                        frequency = RecurringFrequency.MONTHLY,
                        interval = 1,
                    ),
                    nextRunEpochMillis = System.currentTimeMillis(),
                    note = state.recurringNoteInput.trim().ifBlank { null },
                ),
            ).onSuccess {
                _uiState.update { it.copy(recurringAmountInput = "", recurringNoteInput = "", infoMessage = "Đã tạo giao dịch định kỳ.") }
                refresh()
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.userMessage()) }
            }
        }
    }

    fun createFriendRequest() {
        val friendUserId = _uiState.value.friendUserIdInput
        if (friendUserId.isBlank() || friendUserId == userProfile.id) {
            _uiState.update { it.copy(errorMessage = "Nhập user id hợp lệ của người bạn muốn thêm.") }
            return
        }
        viewModelScope.launch {
            sharingRepository.createFriend(
                Friend(
                    id = UUID.randomUUID().toString(),
                    userId = userProfile.id,
                    friendUserId = friendUserId,
                    status = FriendStatus.PENDING,
                ),
            ).onSuccess {
                _uiState.update { it.copy(friendUserIdInput = "", infoMessage = "Đã gửi lời mời bạn bè.") }
                refresh()
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.userMessage()) }
            }
        }
    }

    fun createGroup() {
        val name = _uiState.value.groupNameInput.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nhập tên nhóm.") }
            return
        }
        viewModelScope.launch {
            sharingRepository.createGroup(
                Group(
                    id = UUID.randomUUID().toString(),
                    ownerUserId = userProfile.id,
                    name = name,
                    icon = null,
                ),
            ).onSuccess {
                _uiState.update { it.copy(groupNameInput = "", infoMessage = "Đã tạo nhóm.") }
                refresh()
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.userMessage()) }
            }
        }
    }

    fun shareLatestTransactionToGroup() {
        val state = _uiState.value
        val transaction = state.recentTransactions.firstOrNull()
        val groupId = state.selectedShareGroupId
        if (transaction == null || groupId == null) {
            _uiState.update { it.copy(errorMessage = "Cần có giao dịch và nhóm trước khi chia sẻ.") }
            return
        }
        viewModelScope.launch {
            sharingRepository.shareTransaction(
                SharedTransaction(
                    id = UUID.randomUUID().toString(),
                    transactionId = transaction.id,
                    sharedByUserId = userProfile.id,
                    groupId = groupId,
                ),
            ).onSuccess {
                _uiState.update { it.copy(infoMessage = "Đã chia sẻ giao dịch gần nhất vào nhóm.") }
                refresh()
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.userMessage()) }
            }
        }
    }

    fun deleteSharedTransaction(id: String) {
        viewModelScope.launch {
            sharingRepository.deleteSharedTransaction(id)
                .onSuccess { refresh() }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.userMessage()) } }
        }
    }

    fun archiveRecurringTransaction(id: String) {
        viewModelScope.launch {
            recurringTransactionRepository.archiveRecurringTransaction(id)
                .onSuccess { refresh() }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.userMessage()) } }
        }
    }

    private fun updatePreference(transform: (UserPreference) -> UserPreference) {
        val current = _uiState.value.preference ?: UserPreference(userId = userProfile.id)
        viewModelScope.launch {
            userPreferenceRepository.updatePreferences(transform(current))
                .onSuccess { updated ->
                    _uiState.update { it.copy(preference = updated, infoMessage = "Đã lưu cài đặt.") }
                }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.userMessage()) } }
        }
    }
}
