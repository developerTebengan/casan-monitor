package id.casan.monitor.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.casan.monitor.data.repository.AuthError
import id.casan.monitor.data.repository.AuthRepository
import id.casan.monitor.data.repository.AuthState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(val loading: Boolean = false, val error: AuthError? = null)

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    val session = repository.state
    private val mutableUi = MutableStateFlow(AuthUiState())
    val ui = mutableUi.asStateFlow()
    private var sessionCheck: Job? = null

    init {
        restore()
        viewModelScope.launch {
            session.collectLatest { state ->
                if (state is AuthState.SignedIn) {
                    while (true) {
                        val remaining = state.expiresAt - System.currentTimeMillis()
                        if (remaining <= 0) break
                        delay(remaining.coerceAtMost(60_000))
                    }
                    restore()
                }
            }
        }
    }

    fun restore() {
        if (sessionCheck?.isActive == true) return
        sessionCheck = viewModelScope.launch { repository.restore() }
    }

    fun login(email: String, password: String) {
        if (mutableUi.value.loading) return
        mutableUi.value = AuthUiState(loading = true)
        viewModelScope.launch {
            mutableUi.value = AuthUiState(error = repository.login(email, password))
        }
    }

    fun logout() {
        mutableUi.value = AuthUiState()
        viewModelScope.launch { repository.logout() }
    }
}

fun AuthError.message(): String = when (this) {
    AuthError.CONFIGURATION -> "This build has no CASAN backend configured."
    AuthError.CREDENTIALS -> "Check your CASAN admin email and password."
    AuthError.TIMEOUT -> "The server took too long to respond. Try again."
    AuthError.NETWORK -> "Could not reach CASAN. Check your connection and try again."
    AuthError.SERVER -> "CASAN could not process the request. Try again."
    AuthError.STORAGE -> "This device could not securely save the session. Try again."
}
