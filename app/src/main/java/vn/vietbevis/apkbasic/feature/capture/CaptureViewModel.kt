package vn.vietbevis.apkbasic.feature.capture

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
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.model.Wallet
import vn.vietbevis.apkbasic.domain.repository.CategoryRepository
import vn.vietbevis.apkbasic.domain.repository.PhotoRepository
import vn.vietbevis.apkbasic.domain.repository.TransactionRepository
import vn.vietbevis.apkbasic.domain.repository.WalletRepository
import vn.vietbevis.apkbasic.domain.validation.TransactionValidator
import java.util.UUID

data class CaptureUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val wallets: List<Wallet> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedPhotoPath: String? = null,
    val amountInput: String = "",
    val calculatorState: CalculatorState = CalculatorState(),
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedWalletId: String? = null,
    val selectedCategoryId: String? = null,
    val noteInput: String = "",
    val occurredAtEpochMillis: Long = System.currentTimeMillis(),
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val canSaveWithoutPhoto: Boolean = false,
)

class CaptureViewModel(
    private val userProfile: UserProfile,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val photoRepository: PhotoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    init {
        loadFormData()
    }

    fun loadFormData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val walletsResult = walletRepository.listWallets(includeArchived = false)
            val categoriesResult = categoryRepository.listCategories()

            val wallets = walletsResult.getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
                return@launch
            }
            val categories = categoriesResult.getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
                return@launch
            }
            val type = _uiState.value.type
            _uiState.update {
                it.copy(
                    isLoading = false,
                    wallets = wallets,
                    categories = categories,
                    selectedWalletId = it.selectedWalletId ?: wallets.firstOrNull()?.id,
                    selectedCategoryId = it.selectedCategoryId
                        ?: categories.firstOrNull { category -> category.transactionType == type }?.id,
                    errorMessage = null,
                )
            }
        }
    }

    fun onPhotoCaptured(path: String) {
        _uiState.update {
            it.copy(selectedPhotoPath = path, infoMessage = null, errorMessage = null, canSaveWithoutPhoto = false)
        }
    }

    fun onPhotoCaptureFailed() {
        _uiState.update {
            it.copy(errorMessage = "Không chụp được ảnh. Vui lòng thử lại.", canSaveWithoutPhoto = false)
        }
    }

    fun retakePhoto() {
        _uiState.update {
            it.copy(selectedPhotoPath = null, infoMessage = null, errorMessage = null, canSaveWithoutPhoto = false)
        }
    }

    fun onAmountChange(value: String) {
        _uiState.update {
            it.copy(
                amountInput = value.filter(Char::isDigit),
                calculatorState = CalculatorState(display = value.filter(Char::isDigit).ifBlank { "0" }),
                errorMessage = null,
            )
        }
    }

    fun onCalculatorKey(key: CalculatorKey) {
        _uiState.update { state ->
            val calculatorState = CalculatorEngine.press(state.calculatorState, key)
            state.copy(
                calculatorState = calculatorState,
                amountInput = calculatorState.amountInput,
                errorMessage = null,
            )
        }
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.update { state ->
            val firstMatchingCategory = state.categories.firstOrNull { it.transactionType == type }?.id
            state.copy(type = type, selectedCategoryId = firstMatchingCategory, errorMessage = null)
        }
    }

    fun onWalletSelected(walletId: String) {
        _uiState.update { it.copy(selectedWalletId = walletId, errorMessage = null) }
    }

    fun onCategorySelected(categoryId: String) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, errorMessage = null) }
    }

    fun onNoteChange(value: String) {
        _uiState.update { it.copy(noteInput = value, errorMessage = null) }
    }

    fun resetOccurredAtToNow() {
        _uiState.update { it.copy(occurredAtEpochMillis = System.currentTimeMillis()) }
    }

    fun save() {
        saveInternal(allowPhotoSkip = false)
    }

    fun saveWithoutPhoto() {
        saveInternal(allowPhotoSkip = true)
    }

    private fun saveInternal(allowPhotoSkip: Boolean) {
        val state = uiState.value
        val amount = state.amountInput.toLongOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(errorMessage = "Nhập số tiền lớn hơn 0.") }
            return
        }
        val wallet = state.wallets.firstOrNull { it.id == state.selectedWalletId }
        val category = state.categories.firstOrNull { it.id == state.selectedCategoryId }
        if (category == null) {
            _uiState.update { it.copy(errorMessage = "Chọn danh mục.") }
            return
        }

        val transactionId = UUID.randomUUID().toString()
        val baseTransaction = Transaction(
            id = transactionId,
            userId = userProfile.id,
            walletId = wallet?.id.orEmpty(),
            categoryId = category.id,
            type = state.type,
            amount = Money.vnd(amount),
            note = state.noteInput.trim().ifBlank { null },
            occurredAtEpochMillis = state.occurredAtEpochMillis,
            photoPath = null,
        )
        val validationErrors = TransactionValidator.validate(baseTransaction, wallet, category)
        if (validationErrors.isNotEmpty()) {
            _uiState.update { it.copy(errorMessage = "Thông tin giao dịch chưa hợp lệ.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, infoMessage = null) }
            var uploadedPhotoPath: String? = null

            if (!allowPhotoSkip && state.selectedPhotoPath != null) {
                uploadedPhotoPath = photoRepository.uploadTransactionPhoto(
                    userId = userProfile.id,
                    transactionId = transactionId,
                    localPath = state.selectedPhotoPath,
                ).getOrElse { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "Upload ảnh thất bại. Bạn có thể thử lại hoặc lưu không ảnh.",
                            canSaveWithoutPhoto = true,
                        )
                    }
                    return@launch
                }
            }

            val transaction = baseTransaction.copy(photoPath = uploadedPhotoPath)
            transactionRepository.createTransaction(transaction)
                .onSuccess {
                    _uiState.value = CaptureUiState(
                        isLoading = false,
                        wallets = state.wallets,
                        categories = state.categories,
                        selectedWalletId = state.selectedWalletId,
                        selectedCategoryId = state.selectedCategoryId,
                        infoMessage = "Đã lưu giao dịch.",
                    )
                }
                .onFailure { error ->
                    uploadedPhotoPath?.let { photoRepository.deleteTransactionPhoto(it) }
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = error.userMessage())
                    }
                }
        }
    }
}
