package org.hamersztein.klox.scanner

import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType
import org.hamersztein.klox.token.TokenType.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

class ScannerTest {

    @Test
    fun `should return EOF token when program is empty`() {
        val scanner = Scanner("")
        val tokens = scanner.scanTokens()

        assertEquals(1, tokens.size)
        assertEquals(EOF, tokens[0].type)
    }

    @MethodSource("provideStringToTokenArgs")
    @ParameterizedTest(name = "should convert {0} character into {1} token with null literal")
    fun `should convert string with character into expected token type`(input: String, expectedTokenType: TokenType) {
        val scanner = Scanner(input)
        val tokens = scanner.scanTokens()

        assertEquals(2, tokens.size)

        val expectedToken = Token(type = expectedTokenType, lexeme = input, literal = null, line = 1)
        assertEquals(expectedToken, tokens[0])

        assertEquals(EOF, tokens[1].type)
    }

    @ParameterizedTest
    @MethodSource("provideKeyWordsArgs")
    fun `should add a token for keywords`(input: String, expectedTokenType: TokenType) {
        val scanner = Scanner(input)
        val tokens = scanner.scanTokens()

        assertEquals(2, tokens.size)

        val expectedToken = Token(type = expectedTokenType, lexeme = input, literal = null, line = 1)
        assertEquals(expectedToken, tokens[0])

        assertEquals(EOF, tokens[1].type)
    }

    @Test
    fun `should add a token for an identifier when word is not a keyword`() {
        val scanner = Scanner("something")
        val tokens = scanner.scanTokens()

        assertEquals(2, tokens.size)

        val expectedToken = Token(type = IDENTIFIER, lexeme = "something", literal = null, line = 1)
        assertEquals(expectedToken, tokens[0])

        assertEquals(EOF, tokens[1].type)
    }

    @Test
    fun `should add string token for a string value`() {
        val scanner = Scanner("\"some string value\"")
        val tokens = scanner.scanTokens()

        assertEquals(2, tokens.size)

        val expectedToken = Token(
            type = STRING,
            lexeme = "\"some string value\"",
            literal = "some string value",
            line = 1
        )
        
        assertEquals(expectedToken, tokens[0])

        assertEquals(EOF, tokens[1].type)
    }

    @ValueSource(strings = ["1", "1.1"])
    @ParameterizedTest(name = "should add number token and convert value to double when input is {0}")
    fun `should add number token for numerical values`(input: String) {
        val scanner = Scanner(input)
        val tokens = scanner.scanTokens()

        assertEquals(2, tokens.size)

        val expectedToken = Token(type = NUMBER, lexeme = input, literal = input.toDouble(), line = 1)
        assertEquals(expectedToken, tokens[0])

        assertEquals(EOF, tokens[1].type)
    }

    @Test
    fun `should not add a token for a comment`() {
        val scanner = Scanner("// some comment")
        val tokens = scanner.scanTokens()

        assertEquals(1, tokens.size)
        assertEquals(EOF, tokens[0].type)
    }

    @Test
    fun `should include correct line number for a token`() {
        val program = """
            // some comment
            1
        """.trimIndent()

        val scanner = Scanner(program)
        val tokens = scanner.scanTokens()

        assertEquals(2, tokens.size)

        assertEquals(2, tokens[0].line)

        assertEquals(EOF, tokens[1].type)
    }

    @Test
    fun `should handle multiline comments`() {
        val program = """
            /*
            this is some multiline comment
            */
            1
        """.trimIndent()

        val scanner = Scanner(program)
        val tokens = scanner.scanTokens()

        assertEquals(2, tokens.size)

        val expectedToken = Token(type = NUMBER, lexeme = "1", literal = 1.0, line = 4)
        assertEquals(expectedToken, tokens[0])

        assertEquals(EOF, tokens[1].type)
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