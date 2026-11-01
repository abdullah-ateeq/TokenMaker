package com.example.tokenmaker.domain.model

/**
 * Configuration options for generating a secure token.
 */
data class TokenConfig(
    val length: Int = DEFAULT_LENGTH,
    val selectedSets: Set<CharacterSet> = CharacterSet.allSets(),
    val excludeAmbiguous: Boolean = false,
    val requireEachSelectedSet: Boolean = true
) {
    companion object {
        const val MIN_LENGTH = 8
        const val MAX_LENGTH = 128
        const val DEFAULT_LENGTH = 16
    }
}

/**
 * Result of validating token configuration.
 */
sealed interface TokenValidationResult {
    data object Valid : TokenValidationResult
    data class Invalid(val message: String) : TokenValidationResult
}
