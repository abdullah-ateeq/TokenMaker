package com.example.tokenmaker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tokenmaker.domain.model.CharacterSet
import com.example.tokenmaker.domain.model.TokenConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tokenmaker_settings")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DEFAULT_LENGTH = intPreferencesKey("default_length")
        val DEFAULT_SELECTED_SETS = stringSetPreferencesKey("default_selected_sets")
        val DEFAULT_EXCLUDE_AMBIGUOUS = booleanPreferencesKey("default_exclude_ambiguous")
        val DEFAULT_REQUIRE_EACH_SET = booleanPreferencesKey("default_require_each_set")
        val HIDE_TOKENS_BY_DEFAULT = booleanPreferencesKey("hide_tokens_by_default")
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        val CLEAR_TOKEN_ON_LEAVE = booleanPreferencesKey("clear_token_on_leave")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeModeString = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val themeMode = runCatching { ThemeMode.valueOf(themeModeString) }.getOrDefault(ThemeMode.SYSTEM)

            val defaultLength = preferences[PreferencesKeys.DEFAULT_LENGTH] ?: TokenConfig.DEFAULT_LENGTH
            val selectedSetIds = preferences[PreferencesKeys.DEFAULT_SELECTED_SETS]
            val defaultSelectedSets = if (selectedSetIds != null) {
                selectedSetIds.mapNotNull { CharacterSet.fromId(it) }.toSet().ifEmpty { CharacterSet.allSets() }
            } else {
                CharacterSet.allSets()
            }

            val defaultExcludeAmbiguous = preferences[PreferencesKeys.DEFAULT_EXCLUDE_AMBIGUOUS] ?: false
            val defaultRequireEachSet = preferences[PreferencesKeys.DEFAULT_REQUIRE_EACH_SET] ?: true
            val hideTokensByDefault = preferences[PreferencesKeys.HIDE_TOKENS_BY_DEFAULT] ?: false
            val hapticFeedbackEnabled = preferences[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] ?: true
            val clearTokenOnLeave = preferences[PreferencesKeys.CLEAR_TOKEN_ON_LEAVE] ?: false

            UserPreferences(
                themeMode = themeMode,
                defaultLength = defaultLength.coerceIn(TokenConfig.MIN_LENGTH, TokenConfig.MAX_LENGTH),
                defaultSelectedSets = defaultSelectedSets,
                defaultExcludeAmbiguous = defaultExcludeAmbiguous,
                defaultRequireEachSet = defaultRequireEachSet,
                hideTokensByDefault = hideTokensByDefault,
                hapticFeedbackEnabled = hapticFeedbackEnabled,
                clearTokenOnLeave = clearTokenOnLeave
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setDefaultLength(length: Int) {
        val clamped = length.coerceIn(TokenConfig.MIN_LENGTH, TokenConfig.MAX_LENGTH)
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_LENGTH] = clamped
        }
    }

    suspend fun setDefaultSelectedSets(sets: Set<CharacterSet>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_SELECTED_SETS] = sets.map { it.id }.toSet()
        }
    }

    suspend fun setDefaultExcludeAmbiguous(exclude: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_EXCLUDE_AMBIGUOUS] = exclude
        }
    }

    suspend fun setDefaultRequireEachSet(requireEach: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_REQUIRE_EACH_SET] = requireEach
        }
    }

    suspend fun setHideTokensByDefault(hide: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_TOKENS_BY_DEFAULT] = hide
        }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }

    suspend fun setClearTokenOnLeave(clear: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLEAR_TOKEN_ON_LEAVE] = clear
        }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
