package com.smarttrip.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrip.app.data.remote.models.UserDto
import com.smarttrip.app.data.repository.AuthRepository
import com.smarttrip.app.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Loading : AuthUiState()
    object Idle : AuthUiState()
    data class Authenticated(val user: UserDto) : AuthUiState()
    object Unauthenticated : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    data class RequiresVerification(val email: String) : AuthUiState()
    data class NeedsEmailInput(val email: String) : AuthUiState() // après register
    object EmailVerified : AuthUiState() // email vérifié, rediriger vers login
    object Guest : AuthUiState()         // navigation sans compte
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()

    init {
        checkSession()
    }

    // ─── Vérifier si une session existe au démarrage ──────────────────────
    fun checkSession() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.getMe()) {
                is AuthResult.Success -> {
                    _currentUser.value = result.data
                    _uiState.value = AuthUiState.Authenticated(result.data)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.Unauthenticated
                }
                AuthResult.RequiresVerification -> {
                    _uiState.value = AuthUiState.Unauthenticated
                }
            }
        }
    }

    // ─── Connexion ────────────────────────────────────────────────────────
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    _currentUser.value = result.data
                    _uiState.value = AuthUiState.Authenticated(result.data)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                AuthResult.RequiresVerification -> {
                    _uiState.value = AuthUiState.RequiresVerification(email)
                }
            }
        }
    }

    // ─── Inscription ──────────────────────────────────────────────────────
    fun register(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.register(email, password, firstName, lastName)) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState.NeedsEmailInput(result.data)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                AuthResult.RequiresVerification -> {
                    _uiState.value = AuthUiState.RequiresVerification(email)
                }
            }
        }
    }

    // ─── Vérification email ───────────────────────────────────────────────
    fun verifyEmail(email: String, code: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (authRepository.verifyEmail(email, code)) {
                is AuthResult.Success -> {
                    // Ne devrait pas arriver (backend ne retourne pas de user ici)
                    _uiState.value = AuthUiState.EmailVerified
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.Error("Code invalide ou expiré")
                }
                AuthResult.RequiresVerification -> {
                    // Backend a confirmé la vérification, rediriger vers login
                    _uiState.value = AuthUiState.EmailVerified
                }
            }
        }
    }

    // ─── Suppression compte (timer expiré) ───────────────────────────────
    fun deleteAccount(onDeleted: () -> Unit) {
        viewModelScope.launch {
            authRepository.deleteAccount()
            _currentUser.value = null
            _uiState.value = AuthUiState.Unauthenticated
            onDeleted()
        }
    }

    // ─── Renvoi code vérif ────────────────────────────────────────────────
    fun resendVerification(email: String) {
        viewModelScope.launch {
            authRepository.resendVerification(email)
        }
    }

    // ─── Mot de passe oublié ──────────────────────────────────────────────
    fun forgotPassword(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val result = authRepository.forgotPassword(email)) {
                is AuthResult.Success -> onResult(true, result.data)
                is AuthResult.Error -> onResult(false, result.message)
                else -> onResult(false, "Erreur")
            }
        }
    }

    // ─── Réinitialiser mot de passe ───────────────────────────────────────
    fun resetPassword(token: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val result = authRepository.resetPassword(token, password)) {
                is AuthResult.Success -> onResult(true, result.data)
                is AuthResult.Error -> onResult(false, result.message)
                else -> onResult(false, "Erreur")
            }
        }
    }

    // ─── Déconnexion ──────────────────────────────────────────────────────
    fun continueAsGuest() {
        _uiState.value = AuthUiState.Guest
    }

    // ─── Déconnexion ──────────────────────────────────────────────────────
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _currentUser.value = null
            _uiState.value = AuthUiState.Unauthenticated
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
