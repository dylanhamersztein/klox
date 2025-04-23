package org.hamersztein.klox.scanner

import org.hamersztein.klox.scanner.ScannerTestUtils.assertProgram
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType
import org.hamersztein.klox.token.TokenType.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class ScannerTest {

    @Test
    fun `should return EOF token when program is empty`() {
        assertProgram("") {
            assertEquals(1, it.size)
            assertEquals(EOF, it[0].type)
        }
    }

    @MethodSource("provideStringToTokenArgs")
    @ParameterizedTest(name = "should convert {0} character into {1} token with null literal")
    fun `should convert string with character into expected token type`(program: String, expectedTokenType: TokenType) {
        assertProgram(program) {
            assertEquals(2, it.size)

            val expectedToken = Token(type = expectedTokenType, lexeme = program, literal = null, line = 1)
            assertEquals(expectedToken, it[0])

            assertEquals(EOF, it[1].type)
        }
    }

    @ParameterizedTest
    @MethodSource("provideKeyWordsArgs")
    fun `should add a token for keywords`(input: String, expectedTokenType: TokenType) {
        assertProgram(input) {
            assertEquals(2, it.size)

            val expectedToken = Token(type = expectedTokenType, lexeme = input, literal = null, line = 1)
            assertEquals(expectedToken, it[0])

            assertEquals(EOF, it[1].type)
        }
    }

    @Test
    fun `should add a token for an identifier when word is not a keyword`() {
        assertProgram("something") {
            assertEquals(2, it.size)

            val expectedToken = Token(type = IDENTIFIER, lexeme = "something", literal = null, line = 1)
            assertEquals(expectedToken, it[0])

            assertEquals(EOF, it[1].type)
        }
    }

    companion object {
        @JvmStatic
        private fun provideStringToTokenArgs() = listOf(
            Arguments.of("(", LEFT_PAREN),
            Arguments.of(")", RIGHT_PAREN),
            Arguments.of("{", LEFT_BRACE),
            Arguments.of("}", RIGHT_BRACE),
            Arguments.of(",", COMMA),
            Arguments.of(".", DOT),
            Arguments.of("-", MINUS),
            Arguments.of("+", PLUS),
            Arguments.of(";", SEMICOLON),
            Arguments.of("*", STAR),
            Arguments.of("!", BANG),
            Arguments.of("!=", BANG_EQUAL),
            Arguments.of("=", EQUAL),
            Arguments.of("==", EQUAL_EQUAL),
            Arguments.of(">", GREATER),
            Arguments.of(">=", GREATER_EQUAL),
            Arguments.of("<", LESS),
            Arguments.of("<=", LESS_EQUAL),
            Arguments.of("<=", LESS_EQUAL),
            Arguments.of("/", SLASH),
            Arguments.of("?", QUESTION_MARK),
            Arguments.of(":", COLON),
        )

        @JvmStatic
        private fun provideKeyWordsArgs() = listOf(
            Arguments.of("and", AND),
            Arguments.of("class", CLASS),
            Arguments.of("if", IF),
            Arguments.of("else", ELSE),
            Arguments.of("false", FALSE),
            Arguments.of("for", FOR),
            Arguments.of("fun", FUN),
            Arguments.of("nil", NIL),
            Arguments.of("or", OR),
            Arguments.of("print", PRINT),
            Arguments.of("return", RETURN),
            Arguments.of("super", SUPER),
            Arguments.of("this", THIS),
            Arguments.of("true", TRUE),
            Arguments.of("var", VAR),
            Arguments.of("while", WHILE)
        )
    }
}
