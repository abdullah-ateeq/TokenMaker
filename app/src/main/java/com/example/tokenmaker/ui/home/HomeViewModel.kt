package com.example.tokenmaker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tokenmaker.data.preferences.UserPreferencesRepository
import com.example.tokenmaker.domain.generator.SecureTokenGenerator
import com.example.tokenmaker.domain.generator.TokenGenerator
import com.example.tokenmaker.domain.model.CharacterSet
import com.example.tokenmaker.domain.model.TokenConfig
import com.example.tokenmaker.domain.model.TokenValidationResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val token: String = "",
    val isTokenVisible: Boolean = true,
    val length: Int = TokenConfig.DEFAULT_LENGTH,
    val selectedSets: Set<CharacterSet> = CharacterSet.allSets(),
    val excludeAmbiguous: Boolean = false,
    val requireEachSet: Boolean = true,
    val validationError: String? = null,
    val hapticEnabled: Boolean = true,
    val clearOnLeave: Boolean = false,
    val isInitialized: Boolean = false
) {
    val canGenerate: Boolean
        get() = validationError == null

    val selectedSummaryText: String
        get() {
            if (selectedSets.isEmpty()) return "None"
            val labels = mutableListOf<String>()
            if (CharacterSet.LOWERCASE in selectedSets) labels.add("a-z")
            if (CharacterSet.UPPERCASE in selectedSets) labels.add("A-Z")
            if (CharacterSet.NUMBERS in selectedSets) labels.add("0-9")
            if (CharacterSet.SPECIAL in selectedSets) labels.add("Special")
            return labels.joinToString(", ")
        }
}

sealed interface HomeEvent {
    data class ShowToast(val message: String) : HomeEvent
    data object TokenCopied : HomeEvent
}

class HomeViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val tokenGenerator: TokenGenerator = SecureTokenGenerator()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            // Load user preferences once to initialize defaults
            val prefs = preferencesRepository.userPreferencesFlow.first()
            _uiState.update { current ->
                current.copy(
                    length = prefs.defaultLength,
                    selectedSets = prefs.defaultSelectedSets,
                    excludeAmbiguous = prefs.defaultExcludeAmbiguous,
                    requireEachSet = prefs.defaultRequireEachSet,
                    isTokenVisible = !prefs.hideTokensByDefault,
                    hapticEnabled = prefs.hapticFeedbackEnabled,
                    clearOnLeave = prefs.clearTokenOnLeave,
                    isInitialized = true
                )
            }
            validateAndGenerate(initial = true)

            // Continue listening for settings changes (like haptics or clearOnLeave)
            preferencesRepository.userPreferencesFlow.collect { updatedPrefs ->
                _uiState.update { current ->
                    current.copy(
                        hapticEnabled = updatedPrefs.hapticFeedbackEnabled,
                        clearOnLeave = updatedPrefs.clearTokenOnLeave
                    )
                }
            }
        }
    }

    fun onLengthChange(newLength: Int) {
        val clamped = newLength.coerceIn(TokenConfig.MIN_LENGTH, TokenConfig.MAX_LENGTH)
        _uiState.update { it.copy(length = clamped) }
        revalidate()
    }

    fun onToggleSet(set: CharacterSet) {
        _uiState.update { current ->
            val updated = if (set in current.selectedSets) {
                current.selectedSets - set
            } else {
                current.selectedSets + set
            }
            current.copy(selectedSets = updated)
        }
        revalidate()
    }

    fun onSelectAllSets() {
        _uiState.update { it.copy(selectedSets = CharacterSet.allSets()) }
        revalidate()
    }

    fun onClearSets() {
        _uiState.update { it.copy(selectedSets = emptySet()) }
        revalidate()
    }

    fun onToggleExcludeAmbiguous() {
        _uiState.update { it.copy(excludeAmbiguous = !it.excludeAmbiguous) }
        revalidate()
    }

    fun onToggleRequireEachSet() {
        _uiState.update { it.copy(requireEachSet = !it.requireEachSet) }
        revalidate()
    }

    fun onToggleVisibility() {
        _uiState.update { it.copy(isTokenVisible = !it.isTokenVisible) }
    }

    fun onGenerateClick() {
        validateAndGenerate(initial = false)
    }

    fun onTokenCopied() {
        viewModelScope.launch {
            _events.emit(HomeEvent.TokenCopied)
        }
    }

    /**
     * Clears sensitive token state when app is put to background
     * if the clear-on-leave setting is active.
     */
    fun onAppLeave() {
        if (_uiState.value.clearOnLeave) {
            _uiState.update { it.copy(token = "") }
        }
    }

    private fun revalidate() {
        val state = _uiState.value
        val config = TokenConfig(
            length = state.length,
            selectedSets = state.selectedSets,
            excludeAmbiguous = state.excludeAmbiguous,
            requireEachSelectedSet = state.requireEachSet
        )
        val validation = tokenGenerator.validate(config)
        _uiState.update {
            it.copy(
                validationError = when (validation) {
                    is TokenValidationResult.Valid -> null
                    is TokenValidationResult.Invalid -> validation.message
                }
            )
        }
    }

    private fun validateAndGenerate(initial: Boolean) {
        val state = _uiState.value
        val config = TokenConfig(
            length = state.length,
            selectedSets = state.selectedSets,
            excludeAmbiguous = state.excludeAmbiguous,
            requireEachSelectedSet = state.requireEachSet
        )
        when (val validation = tokenGenerator.validate(config)) {
            is TokenValidationResult.Valid -> {
                val newToken = tokenGenerator.generate(config)
                _uiState.update {
                    it.copy(
                        token = newToken,
                        validationError = null
                    )
                }
            }
            is TokenValidationResult.Invalid -> {
                _uiState.update {
                    it.copy(
                        validationError = validation.message
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            preferencesRepository: UserPreferencesRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(preferencesRepository) as T
            }
        }
    }
}
