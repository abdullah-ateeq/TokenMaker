package com.example.tokenmaker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tokenmaker.data.preferences.ThemeMode
import com.example.tokenmaker.data.preferences.UserPreferences
import com.example.tokenmaker.data.preferences.UserPreferencesRepository
import com.example.tokenmaker.domain.model.CharacterSet
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setDefaultLength(length: Int) {
        viewModelScope.launch {
            preferencesRepository.setDefaultLength(length)
        }
    }

    fun toggleDefaultSet(set: CharacterSet) {
        viewModelScope.launch {
            val current = preferences.value.defaultSelectedSets
            val updated = if (set in current) {
                // Ensure at least one set remains selected
                if (current.size > 1) current - set else current
            } else {
                current + set
            }
            preferencesRepository.setDefaultSelectedSets(updated)
        }
    }

    fun setExcludeAmbiguousByDefault(exclude: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDefaultExcludeAmbiguous(exclude)
        }
    }

    fun setRequireEachSetByDefault(requireEach: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDefaultRequireEachSet(requireEach)
        }
    }

    fun setHideTokensByDefault(hide: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHideTokensByDefault(hide)
        }
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHapticFeedbackEnabled(enabled)
        }
    }

    fun setClearTokenOnLeave(clear: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setClearTokenOnLeave(clear)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            preferencesRepository.resetToDefaults()
        }
    }

    companion object {
        fun provideFactory(
            preferencesRepository: UserPreferencesRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(preferencesRepository) as T
            }
        }
    }
}
