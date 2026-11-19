package id.casan.monitor.feature.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import id.casan.monitor.data.local.ThemeMode
import id.casan.monitor.data.local.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val preferences: ThemePreferences) : ViewModel() {
    private val mutableTheme = MutableStateFlow(preferences.read())
    val theme = mutableTheme.asStateFlow()

    fun setTheme(mode: ThemeMode) {
        preferences.save(mode)
        mutableTheme.value = mode
    }
}
