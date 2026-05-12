package vn.vietbevis.apkbasic.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.vietbevis.apkbasic.core.common.userMessage
import vn.vietbevis.apkbasic.data.bootstrap.OnboardingBootstrapper
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.repository.AuthRepository

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isSignUp: Boolean = false,
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val authenticatedProfile: UserProfile? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val onboardingBootstrapper: OnboardingBootstrapper,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        refreshSession()
    }

    fun refreshSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.currentUser()
                .onSuccess { profile ->
                    if (profile == null) {
                        _uiState.update { it.copy(isLoading = false, isAuthenticated = false) }
                    } else {
                        bootstrapAndEnter(profile)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, isAuthenticated = false, errorMessage = error.userMessage())
                    }
                }
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null, infoMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null, infoMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null, infoMessage = null) }
    }

    fun toggleMode() {
        _uiState.update {
            it.copy(
                isSignUp = !it.isSignUp,
                password = "",
                confirmPassword = "",
                errorMessage = null,
                infoMessage = null,
            )
        }
    }

    fun submit() {
        val state = uiState.value
        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            val authResult = if (state.isSignUp) {
                authRepository.signUp(state.email, state.password)
            } else {
                authRepository.signIn(state.email, state.password)
            }

            authResult
                .onSuccess { profile ->
                    if (state.isSignUp) {
                        authRepository.currentUser()
                            .getOrNull()
                            ?.let { bootstrapAndEnter(it) }
                            ?: _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    infoMessage = "Tài khoản đã được tạo. Vui lòng xác nhận email nếu Supabase yêu cầu, rồi đăng nhập.",
                                )
                            }
                    } else {
                        bootstrapAndEnter(profile)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.userMessage())
                    }
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.signOut()
                .onSuccess {
                    _uiState.value = AuthUiState(isLoading = false)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
                }
        }
    }

    private suspend fun bootstrapAndEnter(profile: UserProfile) {
        onboardingBootstrapper.ensureDefaults(profile)
            .onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        authenticatedProfile = profile,
                        errorMessage = null,
                        infoMessage = null,
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, isAuthenticated = false, errorMessage = error.userMessage())
                }
            }
    }

    private fun validate(state: AuthUiState): String? = when {
        state.email.isBlank() -> "Nhập email."
        "@" !in state.email -> "Email không hợp lệ."
        state.password.length < 6 -> "Mật khẩu cần ít nhất 6 ký tự."
        state.isSignUp && state.password != state.confirmPassword -> "Mật khẩu xác nhận không khớp."
        else -> null
    }
}
