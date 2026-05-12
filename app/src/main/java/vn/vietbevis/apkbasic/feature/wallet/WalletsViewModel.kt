package vn.vietbevis.apkbasic.feature.wallet

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
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.model.Wallet
import vn.vietbevis.apkbasic.domain.model.WalletType
import vn.vietbevis.apkbasic.domain.repository.CategoryRepository
import vn.vietbevis.apkbasic.domain.repository.WalletRepository
import java.util.UUID

data class WalletsUiState(
    val isLoading: Boolean = true,
    val wallets: List<Wallet> = emptyList(),
    val categories: List<Category> = emptyList(),
    val walletNameInput: String = "",
    val walletTypeInput: WalletType = WalletType.CASH,
    val categoryNameInput: String = "",
    val categoryTypeInput: TransactionType = TransactionType.EXPENSE,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)

class WalletsViewModel(
    private val userProfile: UserProfile,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WalletsUiState())
    val uiState: StateFlow<WalletsUiState> = _uiState.asStateFlow()

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
            val categories = categoryRepository.listCategories().getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
                return@launch
            }
            _uiState.update { it.copy(isLoading = false, wallets = wallets, categories = categories) }
        }
    }

    fun onWalletNameChange(value: String) {
        _uiState.update { it.copy(walletNameInput = value, errorMessage = null, infoMessage = null) }
    }

    fun onWalletTypeChange(value: WalletType) {
        _uiState.update { it.copy(walletTypeInput = value) }
    }

    fun createWallet() {
        val state = uiState.value
        val name = state.walletNameInput.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nhập tên ví.") }
            return
        }
        viewModelScope.launch {
            walletRepository.createWallet(
                Wallet(
                    id = UUID.randomUUID().toString(),
                    userId = userProfile.id,
                    name = name,
                    type = state.walletTypeInput,
                    initialBalance = Money.vnd(0),
                ),
            ).onSuccess {
                _uiState.update { state -> state.copy(walletNameInput = "", infoMessage = "Da tao vi.") }
                refresh()
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.userMessage()) }
            }
        }
    }

    fun archiveWallet(walletId: String) {
        viewModelScope.launch {
            walletRepository.archiveWallet(walletId)
                .onSuccess { refresh() }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.userMessage()) } }
        }
    }

    fun onCategoryNameChange(value: String) {
        _uiState.update { it.copy(categoryNameInput = value, errorMessage = null, infoMessage = null) }
    }

    fun onCategoryTypeChange(value: TransactionType) {
        _uiState.update { it.copy(categoryTypeInput = value) }
    }

    fun createCategory() {
        val state = uiState.value
        val name = state.categoryNameInput.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nhập tên danh mục.") }
            return
        }
        viewModelScope.launch {
            categoryRepository.createCategory(
                Category(
                    id = UUID.randomUUID().toString(),
                    userId = userProfile.id,
                    name = name,
                    transactionType = state.categoryTypeInput,
                    isDefault = false,
                ),
            ).onSuccess {
                _uiState.update { state -> state.copy(categoryNameInput = "", infoMessage = "Da tao danh muc.") }
                refresh()
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.userMessage()) }
            }
        }
    }

    fun archiveCategory(categoryId: String) {
        viewModelScope.launch {
            categoryRepository.archiveCategory(categoryId)
                .onSuccess { refresh() }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.userMessage()) } }
        }
    }
}
