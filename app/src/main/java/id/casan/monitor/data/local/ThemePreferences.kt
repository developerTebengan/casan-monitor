package id.casan.monitor.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Singleton
class ThemePreferences @Inject constructor(@ApplicationContext context: Context) {
    private val preferences = context.getSharedPreferences("monitor_settings", Context.MODE_PRIVATE)

    fun read(): ThemeMode =
        ThemeMode.entries.firstOrNull { it.name == preferences.getString("theme", null) }
            ?: ThemeMode.SYSTEM

    fun save(mode: ThemeMode) {
        preferences.edit().putString("theme", mode.name).apply()
    }
}
