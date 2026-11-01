package com.example.tokenmaker

import com.example.tokenmaker.domain.generator.SecureTokenGenerator
import com.example.tokenmaker.domain.model.CharacterSet
import com.example.tokenmaker.domain.model.TokenConfig
import com.example.tokenmaker.domain.model.TokenValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class SecureTokenGeneratorTest {

    private lateinit var generator: SecureTokenGenerator

    @Before
    fun setUp() {
        generator = SecureTokenGenerator()
    }

    @Test
    fun testTokenLengths() {
        val testLengths = listOf(8, 16, 24, 32, 64, 100, 128)
        for (length in testLengths) {
            val config = TokenConfig(length = length)
            val token = generator.generate(config)
            assertEquals("Generated token length should match config length", length, token.length)
        }
    }

    @Test
    fun testOnlyAllowedCharactersAppear() {
        // Test Lowercase only
        val lowerConfig = TokenConfig(
            length = 32,
            selectedSets = setOf(CharacterSet.LOWERCASE)
        )
        val lowerToken = generator.generate(lowerConfig)
        assertTrue(
            "All characters should be lowercase",
            lowerToken.all { it in CharacterSet.LOWERCASE.characters }
        )

        // Test Uppercase only
        val upperConfig = TokenConfig(
            length = 32,
            selectedSets = setOf(CharacterSet.UPPERCASE)
        )
        val upperToken = generator.generate(upperConfig)
        assertTrue(
            "All characters should be uppercase",
            upperToken.all { it in CharacterSet.UPPERCASE.characters }
        )

        // Test Numbers only
        val numbersConfig = TokenConfig(
            length = 32,
            selectedSets = setOf(CharacterSet.NUMBERS)
        )
        val numbersToken = generator.generate(numbersConfig)
        assertTrue(
            "All characters should be digits",
            numbersToken.all { it in CharacterSet.NUMBERS.characters }
        )

        // Test Special only
        val specialConfig = TokenConfig(
            length = 32,
            selectedSets = setOf(CharacterSet.SPECIAL)
        )
        val specialToken = generator.generate(specialConfig)
        assertTrue(
            "All characters should be special characters",
            specialToken.all { it in CharacterSet.SPECIAL.characters }
        )
    }

    @Test
    fun testEverySelectedCharacterSetRepresentedWhenRequired() {
        val allSets = CharacterSet.allSets()
        val config = TokenConfig(
            length = 16,
            selectedSets = allSets,
            requireEachSelectedSet = true
        )

        // Run multiple generations to ensure requirement consistently holds
        repeat(50) {
            val token = generator.generate(config)

            val hasLower = token.any { it in CharacterSet.LOWERCASE.characters }
            val hasUpper = token.any { it in CharacterSet.UPPERCASE.characters }
            val hasNumber = token.any { it in CharacterSet.NUMBERS.characters }
            val hasSpecial = token.any { it in CharacterSet.SPECIAL.characters }

            assertTrue("Token must contain at least one lowercase letter", hasLower)
            assertTrue("Token must contain at least one uppercase letter", hasUpper)
            assertTrue("Token must contain at least one number", hasNumber)
            assertTrue("Token must contain at least one special character", hasSpecial)
        }
    }

    @Test
    fun testEverySelectedSubsetRepresentedWhenRequired() {
        val sets = setOf(CharacterSet.NUMBERS, CharacterSet.SPECIAL)
        val config = TokenConfig(
            length = 10,
            selectedSets = sets,
            requireEachSelectedSet = true
        )

        repeat(30) {
            val token = generator.generate(config)
            assertTrue("Token must contain at least one digit", token.any { it in CharacterSet.NUMBERS.characters })
            assertTrue("Token must contain at least one special char", token.any { it in CharacterSet.SPECIAL.characters })
            assertFalse("Token must not contain lowercase letters", token.any { it in CharacterSet.LOWERCASE.characters })
            assertFalse("Token must not contain uppercase letters", token.any { it in CharacterSet.UPPERCASE.characters })
        }
    }

    @Test
    fun testAmbiguousCharactersExclusion() {
        val ambiguousChars = CharacterSet.AMBIGUOUS_CHARACTERS // '0', 'O', '1', 'I', 'l'
        val config = TokenConfig(
            length = 128,
            selectedSets = CharacterSet.allSets(),
            excludeAmbiguous = true
        )

        repeat(20) {
            val token = generator.generate(config)
            for (ambiguous in ambiguousChars) {
                assertFalse(
                    "Token must not contain ambiguous character '$ambiguous'",
                    token.contains(ambiguous)
                )
            }
        }
    }

    @Test
    fun testAmbiguousCharactersCanBeIncludedWhenNotExcluded() {
        val config = TokenConfig(
            length = 128,
            selectedSets = CharacterSet.allSets(),
            excludeAmbiguous = false
        )

        var foundAmbiguous = false
        val ambiguousChars = CharacterSet.AMBIGUOUS_CHARACTERS

        for (i in 0 until 20) {
            val token = generator.generate(config)
            if (token.any { it in ambiguousChars }) {
                foundAmbiguous = true
                break
            }
        }

        assertTrue("Ambiguous characters should naturally appear when exclusion is disabled", foundAmbiguous)
    }

    @Test
    fun testEmptyCharacterSetValidation() {
        val config = TokenConfig(
            length = 16,
            selectedSets = emptySet()
        )

        val validation = generator.validate(config)
        assertTrue("Validation should fail when character set is empty", validation is TokenValidationResult.Invalid)

        try {
            generator.generate(config)
            fail("Expected IllegalArgumentException when generating with empty character sets")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }

    @Test
    fun testInvalidLengthValidation() {
        // Length below minimum (8)
        val shortConfig = TokenConfig(length = 7)
        assertTrue(generator.validate(shortConfig) is TokenValidationResult.Invalid)

        val negativeConfig = TokenConfig(length = -5)
        assertTrue(generator.validate(negativeConfig) is TokenValidationResult.Invalid)

        // Length above maximum (128)
        val longConfig = TokenConfig(length = 129)
        assertTrue(generator.validate(longConfig) is TokenValidationResult.Invalid)

        // Boundary valid checks
        val minConfig = TokenConfig(length = 8)
        assertTrue(generator.validate(minConfig) is TokenValidationResult.Valid)

        val maxConfig = TokenConfig(length = 128)
        assertTrue(generator.validate(maxConfig) is TokenValidationResult.Valid)
    }

    @Test
    fun testImpossibleRequirementLengthValidation() {
        // If requireEachSelectedSet is true and length < selectedSets.size
        val config = TokenConfig(
            length = 3,
            selectedSets = CharacterSet.allSets(), // 4 sets
            requireEachSelectedSet = true
        )
        val validation = generator.validate(config)
        assertTrue("Validation must fail when length is less than number of required sets", validation is TokenValidationResult.Invalid)
    }

    @Test
    fun testRegenerationProducesFreshTokens() {
        val config = TokenConfig(length = 24, selectedSets = CharacterSet.allSets())
        val generatedTokens = mutableSetOf<String>()

        repeat(100) {
            val token = generator.generate(config)
            assertFalse(
                "Fresh tokens should not repeat across generations",
                generatedTokens.contains(token)
            )
            generatedTokens.add(token)
        }

        assertEquals(100, generatedTokens.size)
    }

    @Test
    fun testShufflingPreventsPositionalPrefixBias() {
        // When requireEachSelectedSet is true, if characters weren't shuffled,
        // position 0 would always be from set 0, position 1 from set 1, etc.
        // We test that position 0 takes characters from different sets over multiple runs.
        val config = TokenConfig(
            length = 16,
            selectedSets = CharacterSet.allSets(),
            requireEachSelectedSet = true
        )

        val firstCharClasses = mutableSetOf<CharacterSet>()
        repeat(100) {
            val token = generator.generate(config)
            val firstChar = token[0]
            val matchedSet = CharacterSet.entries.find { firstChar in it.characters }
            if (matchedSet != null) {
                firstCharClasses.add(matchedSet)
            }
        }

        // Shuffling ensures that the first character comes from multiple different character sets
        assertTrue(
            "Shuffle must randomize character positions so first char is not deterministic",
            firstCharClasses.size > 1
        )
    }
}
