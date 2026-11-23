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

data class AuthUiState(
    val loading: Boolean = false,
    val error: AuthError? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    val session = repository.state
    private val mutableUi = MutableStateFlow(AuthUiState())
    val ui = mutableUi.asStateFlow()
    private var sessionCheck: Job? = null

    init {
        restore()
    }

    suspend fun monitorSessionExpiry() {
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

    fun restore() {
        if (sessionCheck?.isActive == true) return
        sessionCheck = viewModelScope.launch { repository.restore() }
    }

    fun login(email: String, password: String) {
        val current = mutableUi.value
        if (current.loading) return
        mutableUi.value = current.copy(loading = true, error = null)
        viewModelScope.launch {
            val error = repository.login(email, password)
            mutableUi.value = if (error == null) AuthUiState() else current.copy(error = error)
        }
    }

    fun logout() {
        mutableUi.value = AuthUiState()
        viewModelScope.launch { repository.logout() }
    }
}

fun AuthError.message(): String = when (this) {
    AuthError.CONFIGURATION -> "This build has no CASAN backend configured."
    AuthError.CREDENTIALS -> "The email or password is incorrect, or the admin account is inactive."
    AuthError.RATE_LIMIT -> "Too many attempts. Try again later."
    AuthError.FORBIDDEN -> "CASAN rejected this request. Check your details and try again."
    AuthError.TIMEOUT -> "The server took too long to respond. Try again."
    AuthError.NETWORK -> "Could not reach CASAN. Check your connection and try again."
    AuthError.SERVER -> "CASAN could not process the request. Try again."
    AuthError.STORAGE -> "This device could not securely save the session. Try again."
}
