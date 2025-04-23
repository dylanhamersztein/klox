package org.hamersztein.klox.parser

import org.hamersztein.klox.ast.expression.impl.Literal
import org.hamersztein.klox.parser.ParserTestUtil.assertTokensThatProduceExpressionStatement
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType.*
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.contracts.ExperimentalContracts
import kotlin.test.assertEquals

@ExperimentalContracts
class ParserLiteralTest {

    @Test
    fun `should create a literal expression from a string`() {
        val tokens = listOf(
            Token(STRING, "\"hello\"", "hello", 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        assertTokensThatProduceExpressionStatement(tokens) {
            assertTrue(it is Literal)
            with(it as Literal) {
                assertEquals("hello", value)
            }
        }
    }

    @Test
    fun `should create a literal expression from a number`() {
        val tokens = listOf(
            Token(NUMBER, "1", 1, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        assertTokensThatProduceExpressionStatement(tokens) {
            assertTrue(it is Literal)
            with(it as Literal) {
                assertEquals(1, value)
            }
        }
    }

    @Test
    fun `should create a literal expression from nil`() {
        val tokens = listOf(
            Token(NIL, "nil", null, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        assertTokensThatProduceExpressionStatement(tokens) {
            assertTrue(it is Literal)
            with(it as Literal) {
                assertNull(value)
            }
        }
    }

    @ValueSource(booleans = [true, false])
    @ParameterizedTest(name = "should create a literal expression from {0}")
    fun `should create a literal expression from a boolean`(literal: Boolean) {
        val tokenType = if (literal) TRUE else FALSE
        val tokens = listOf(
            Token(tokenType, literal.toString(), literal, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        assertTokensThatProduceExpressionStatement(tokens) {
            assertTrue(it is Literal)
            with(it as Literal) {
                assertEquals(literal, value)
            }
        }
    }

}
