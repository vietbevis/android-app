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
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.model.ThemeMode
import vn.vietbevis.apkbasic.domain.model.UserPreference
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.repository.AuthRepository
import vn.vietbevis.apkbasic.domain.repository.TransactionRepository
import vn.vietbevis.apkbasic.domain.repository.UserPreferenceRepository

data class ProfileUiState(
    val userProfile: UserProfile,
    val isLoading: Boolean = true,
    val transactionCount: Int = 0,
    val income: Money = Money.vnd(0),
    val expense: Money = Money.vnd(0),
    val balance: Money = Money.vnd(0),
    val preference: UserPreference? = null,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
)

class ProfileViewModel(
    private var userProfile: UserProfile,
    private val authRepository: AuthRepository,
    private val photoRepository: vn.vietbevis.apkbasic.domain.repository.PhotoRepository,
    private val transactionRepository: TransactionRepository,
    private val userPreferenceRepository: UserPreferenceRepository,
    private val onProfileUpdated: (UserProfile) -> Unit = {},
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState(userProfile = userProfile))
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
            
            val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.minorUnits }
            val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.minorUnits }
            _uiState.update {
                it.copy(
                    userProfile = userProfile,
                    isLoading = false,
                    transactionCount = transactions.size,
                    income = Money.vnd(income),
                    expense = Money.vnd(expense),
                    balance = Money.vnd(income - expense),
                    preference = preference,
                    errorMessage = null,
                )
            }
        }
    }

    fun updateDisplayName(name: String, context: android.content.Context) {
        val newProfile = userProfile.copy(
            displayName = name.trim().ifBlank { null },
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            authRepository.updateProfile(newProfile)
                .onSuccess { updated ->
                    userProfile = updated
                    _uiState.update { it.copy(userProfile = updated, infoMessage = context.getString(vn.vietbevis.apkbasic.R.string.profile_msg_name_updated)) }
                    onProfileUpdated(updated)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.userMessage()) }
                }
        }
    }

    fun uploadAvatar(bytes: ByteArray, context: android.content.Context) {
        viewModelScope.launch {
            photoRepository.uploadAvatar(userProfile.id, bytes)
                .onSuccess { publicUrl ->
                    val newProfile = userProfile.copy(
                        avatar = publicUrl,
                        updatedAt = System.currentTimeMillis()
                    )
                    authRepository.updateProfile(newProfile)
                        .onSuccess { updated ->
                            userProfile = updated
                            _uiState.update { it.copy(userProfile = updated, infoMessage = context.getString(vn.vietbevis.apkbasic.R.string.profile_msg_avatar_updated)) }
                            onProfileUpdated(updated)
                        }
                        .onFailure { error ->
                            _uiState.update { it.copy(errorMessage = error.userMessage()) }
                        }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = context.getString(vn.vietbevis.apkbasic.R.string.profile_error_upload_avatar, error.userMessage())) }
                }
        }
    }

    fun setLanguage(language: AppLanguage, context: android.content.Context) {
        updatePreference(context) { it.copy(language = language) }
    }

    fun setThemeMode(themeMode: ThemeMode, context: android.content.Context) {
        updatePreference(context) { it.copy(themeMode = themeMode) }
    }

    private fun updatePreference(context: android.content.Context, transform: (UserPreference) -> UserPreference) {
        val current = _uiState.value.preference ?: UserPreference(userId = userProfile.id)
        viewModelScope.launch {
            userPreferenceRepository.updatePreferences(transform(current))
                .onSuccess { updated ->
                    _uiState.update { it.copy(preference = updated, infoMessage = context.getString(vn.vietbevis.apkbasic.R.string.profile_msg_settings_saved)) }
                }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.userMessage()) } }
        }
    }
}
