package org.hamersztein.klox.scanner

import org.hamersztein.klox.scanner.ScannerTestUtils.assertProgram
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class ScannerPrimitiveTest {

    @Test
    fun `should add string token for a string value`() {
        assertProgram("\"some string value\"") {
            assertEquals(2, it.size)

            val expectedToken = Token(
                type = STRING,
                lexeme = "\"some string value\"",
                literal = "some string value",
                line = 1
            )

            assertEquals(expectedToken, it[0])

            assertEquals(EOF, it[1].type)
        }
    }

    @Test
    fun `should scan multiline strings`() {
        val program = """"some 
            |string"""".trimMargin()

        val scanner = Scanner(program)
        val tokens = scanner.scanTokens()

        assertEquals(2, tokens.size)
        assertEquals(Token(STRING, "\"some \nstring\"", "some \nstring", 2), tokens[0])
        assertEquals(Token(EOF, "", null, 2), tokens[1])
    }

    @Test
    fun `should scan number token when number has more than one digit`() {
        val program = "123"
        assertProgram(program) { tokens ->
            assertEquals(2, tokens.size)
            assertEquals(Token(NUMBER, "123", 123.0, 1), tokens[0])
            assertEquals(Token(EOF, "", null, 1), tokens[1])
        }
    }

    @Test
    fun `should scan number token when number has decimal value`() {
        val program = "12.3"
        assertProgram(program) { tokens ->
            assertEquals(2, tokens.size)
            assertEquals(Token(NUMBER, "12.3", 12.3, 1), tokens[0])
            assertEquals(Token(EOF, "", null, 1), tokens[1])
        }
    }

    @ValueSource(strings = ["1", "1.1"])
    @ParameterizedTest(name = "should add number token and convert value to double when input is {0}")
    fun `should add number token for numerical values`(input: String) {
        assertProgram(input) {
            assertEquals(2, it.size)

            val expectedToken = Token(type = NUMBER, lexeme = input, literal = input.toDouble(), line = 1)
            assertEquals(expectedToken, it[0])

            assertEquals(EOF, it[1].type)
        }
    }

}
