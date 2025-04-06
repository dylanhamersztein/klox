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

    @Test
    fun `should not add a token for a comment`() {
        assertProgram("// some comment") {
            assertEquals(1, it.size)
            assertEquals(EOF, it[0].type)
        }
    }

    @Test
    fun `should include correct line number for a token`() {
        val program = """
            // some comment
            1
        """.trimIndent()

        assertProgram(program) {
            assertEquals(2, it.size)

            assertEquals(2, it[0].line)

            assertEquals(EOF, it[1].type)
        }
    }

    @Test
    fun `should handle multiline comments`() {
        val program = """
            /*
            this is some multiline comment
            */
            1
        """.trimIndent()

        assertProgram(program) {
            assertEquals(2, it.size)

            val expectedToken = Token(type = NUMBER, lexeme = "1", literal = 1.0, line = 4)
            assertEquals(expectedToken, it[0])

            assertEquals(EOF, it[1].type)
        }
    }

    private fun assertProgram(program: String, assertFunction: (List<Token>) -> Unit) =
        assertFunction(Scanner(program).scanTokens())

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