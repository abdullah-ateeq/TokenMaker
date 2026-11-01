package com.example.tokenmaker.domain.model

/**
 * Defines character categories available for token generation.
 */
enum class CharacterSet(
    val id: String,
    val displayName: String,
    val sampleDisplay: String,
    val characters: String
) {
    LOWERCASE(
        id = "lowercase",
        displayName = "Lowercase Letters",
        sampleDisplay = "a-z",
        characters = "abcdefghijklmnopqrstuvwxyz"
    ),
    UPPERCASE(
        id = "uppercase",
        displayName = "Uppercase Letters",
        sampleDisplay = "A-Z",
        characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    ),
    NUMBERS(
        id = "numbers",
        displayName = "Numbers",
        sampleDisplay = "0-9",
        characters = "0123456789"
    ),
    SPECIAL(
        id = "special",
        displayName = "Special Characters",
        sampleDisplay = "!@#$%^&*...",
        characters = "!@#$%^&*()_+-=[]{}|;:,.<>?"
    );

    companion object {
        /**
         * Characters commonly prone to visual ambiguity:
         * 0 (digit zero) vs O (capital letter O)
         * 1 (digit one) vs I (capital letter I) vs l (lowercase L)
         */
        val AMBIGUOUS_CHARACTERS: Set<Char> = setOf('0', 'O', '1', 'I', 'l')

        fun fromId(id: String): CharacterSet? = entries.find { it.id == id }

        fun allSets(): Set<CharacterSet> = entries.toSet()
    }

    /**
     * Returns characters belonging to this set, optionally filtering out ambiguous characters.
     */
    fun getFilteredCharacters(excludeAmbiguous: Boolean): String {
        return if (excludeAmbiguous) {
            characters.filter { it !in AMBIGUOUS_CHARACTERS }
        } else {
            characters
        }
    }
}
