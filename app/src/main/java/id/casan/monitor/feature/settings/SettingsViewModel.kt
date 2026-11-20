package id.casan.monitor.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.casan.monitor.data.local.ThemeMode
import id.casan.monitor.data.local.ThemePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val preferences: ThemePreferences) : ViewModel() {
    val theme = preferences.mode.stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch {
            try {
                preferences.save(mode)
            } catch (_: IOException) {
                // Keep the last persisted selection when storage is temporarily unavailable.
            }
        }
    }
}
