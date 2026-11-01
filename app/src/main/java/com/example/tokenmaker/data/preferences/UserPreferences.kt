package com.example.tokenmaker.data.preferences

import com.example.tokenmaker.domain.model.CharacterSet
import com.example.tokenmaker.domain.model.TokenConfig

/**
 * Model representing persistent application preferences.
 * NOTE: Generated tokens are intentionally NEVER stored here.
 */
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultLength: Int = TokenConfig.DEFAULT_LENGTH,
    val defaultSelectedSets: Set<CharacterSet> = CharacterSet.allSets(),
    val defaultExcludeAmbiguous: Boolean = false,
    val defaultRequireEachSet: Boolean = true,
    val hideTokensByDefault: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
    val clearTokenOnLeave: Boolean = false
)
