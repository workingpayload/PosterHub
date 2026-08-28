package com.example.postershub.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "postershub_settings")

/** User-facing app preferences, separate from the favorites DataStore. */
class SettingsStore(private val context: Context) {

    private val useSystemThemeKey = booleanPreferencesKey("use_system_theme")
    private val warnOnMeteredKey = booleanPreferencesKey("warn_on_metered")

    /** false = always use the cinematic dark theme (default); true = follow system light/dark + dynamic color. */
    val useSystemTheme: Flow<Boolean> = context.settingsDataStore.data.map { it[useSystemThemeKey] ?: false }

    /** Whether to confirm before downloading/wallpapering a poster on a metered connection. */
    val warnOnMetered: Flow<Boolean> = context.settingsDataStore.data.map { it[warnOnMeteredKey] ?: true }

    suspend fun setUseSystemTheme(value: Boolean) {
        context.settingsDataStore.edit { it[useSystemThemeKey] = value }
    }

    suspend fun setWarnOnMetered(value: Boolean) {
        context.settingsDataStore.edit { it[warnOnMeteredKey] = value }
    }
}
