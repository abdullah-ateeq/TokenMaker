package com.example.tokenmaker.domain.generator

import com.example.tokenmaker.domain.model.CharacterSet
import com.example.tokenmaker.domain.model.TokenConfig
import com.example.tokenmaker.domain.model.TokenValidationResult
import java.security.SecureRandom

/**
 * Interface defining secure token generation contract.
 */
interface TokenGenerator {
    /**
     * Validates if the configuration can be satisfied.
     */
    fun validate(config: TokenConfig): TokenValidationResult

    /**
     * Generates a secure random token based on [config].
     * Throws [IllegalArgumentException] if [config] is invalid.
     */
    fun generate(config: TokenConfig): String
}

/**
 * Secure token generator adhering to cryptographic best practices:
 * - Powered exclusively by [java.security.SecureRandom].
 * - Avoids modulo bias via rejection-sampled bounded integers.
 * - Enforces minimum-one character per set (when requested) combined with an unbiased
 *   Fisher-Yates shuffle.
 * - Stateless with zero retention of generated tokens.
 */
class SecureTokenGenerator(
    private val secureRandom: SecureRandom = SecureRandom()
) : TokenGenerator {

    override fun validate(config: TokenConfig): TokenValidationResult {
        if (config.selectedSets.isEmpty()) {
            return TokenValidationResult.Invalid("Please select at least one character set.")
        }

        if (config.length < TokenConfig.MIN_LENGTH || config.length > TokenConfig.MAX_LENGTH) {
            return TokenValidationResult.Invalid(
                "Length must be between ${TokenConfig.MIN_LENGTH} and ${TokenConfig.MAX_LENGTH} characters."
            )
        }

        if (config.requireEachSelectedSet && config.length < config.selectedSets.size) {
            return TokenValidationResult.Invalid(
                "Token length (${config.length}) must be at least the number of required sets (${config.selectedSets.size})."
            )
        }

        // Verify each selected set actually has characters after applying ambiguous filter
        for (set in config.selectedSets) {
            val filtered = set.getFilteredCharacters(config.excludeAmbiguous)
            if (filtered.isEmpty()) {
                return TokenValidationResult.Invalid(
                    "Selected set '${set.displayName}' has no remaining characters."
                )
            }
        }

        return TokenValidationResult.Valid
    }

    override fun generate(config: TokenConfig): String {
        val validation = validate(config)
        if (validation is TokenValidationResult.Invalid) {
            throw IllegalArgumentException(validation.message)
        }

        val selectedFilteredSets = config.selectedSets.map { set ->
            set.getFilteredCharacters(config.excludeAmbiguous).toCharArray()
        }

        // Build combined pool of all selected characters
        val totalCapacity = selectedFilteredSets.sumOf { it.size }
        val combinedPool = CharArray(totalCapacity)
        var offset = 0
        for (chars in selectedFilteredSets) {
            System.arraycopy(chars, 0, combinedPool, offset, chars.size)
            offset += chars.size
        }

        val result = CharArray(config.length)
        var generatedCount = 0

        // 1. If required, select at least one character from every enabled set
        if (config.requireEachSelectedSet) {
            for (setChars in selectedFilteredSets) {
                result[generatedCount++] = getRandomChar(setChars)
            }
        }

        // 2. Fill the remaining positions from the combined pool
        while (generatedCount < config.length) {
            result[generatedCount++] = getRandomChar(combinedPool)
        }

        // 3. Perform an unbiased Fisher-Yates shuffle over the entire array
        // to prevent character set positioning bias
        fisherYatesShuffle(result)

        return String(result)
    }

    /**
     * Unbiased random character selection using SecureRandom.nextInt(bound).
     * SecureRandom inherits nextInt(bound) from java.util.Random which uses
     * rejection sampling to eliminate modulo bias.
     */
    private fun getRandomChar(pool: CharArray): Char {
        val index = secureRandom.nextInt(pool.size)
        return pool[index]
    }

    /**
     * In-place cryptographically secure Fisher-Yates shuffle.
     * Uniform distribution for all n! permutations.
     */
    private fun fisherYatesShuffle(array: CharArray) {
        for (i in array.size - 1 downTo 1) {
            val j = secureRandom.nextInt(i + 1)
            val temp = array[i]
            array[i] = array[j]
            array[j] = temp
        }
    }
}
