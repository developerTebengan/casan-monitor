package id.casan.monitor.data.local

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val Context.themeStore by preferencesDataStore(
    name = "monitor_theme",
    produceMigrations = { listOf(SharedPreferencesMigration(it, "monitor_settings", setOf("theme"))) },
)

@Singleton
class ThemePreferences @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.themeStore
    private val key = stringPreferencesKey("theme")

    val mode = store.data.map { values ->
        ThemeMode.entries.firstOrNull { it.name == values[key] } ?: ThemeMode.SYSTEM
    }.catch { failure ->
        if (failure !is IOException) throw failure
        emit(ThemeMode.SYSTEM)
    }

    suspend fun save(mode: ThemeMode) {
        store.edit { it[key] = mode.name }
    }
}
