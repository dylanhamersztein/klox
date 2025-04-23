package org.hamersztein.klox.parser

import org.hamersztein.klox.ast.expression.impl.Literal
import org.hamersztein.klox.ast.expression.impl.Unary
import org.hamersztein.klox.parser.ParserTestUtil.assertTokensThatProduceExpressionStatement
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts
import kotlin.test.assertEquals

@ExperimentalContracts
class ParserUnaryTest {

    @Test
    fun `should create a unary expression from a negation of a boolean`() {
        val tokens = listOf(
            Token(BANG, "!", null, 1),
            Token(FALSE, "false", false, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        assertTokensThatProduceExpressionStatement(tokens) {
            assertTrue(it is Unary)
            with(it as Unary) {
                assertEquals(BANG, operator.type)
                assertTrue(right is Literal)
                with(right as Literal) {
                    assertEquals(false, value)
                }
            }
        }
    }

    @Test
    fun `should create a unary expression from a negation of a number`() {
        val tokens = listOf(
            Token(MINUS, "-", null, 1),
            Token(NUMBER, "2", 2.0, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        assertTokensThatProduceExpressionStatement(tokens) {
            assertTrue(it is Unary)
            with(it as Unary) {
                assertEquals(MINUS, operator.type)
                assertTrue(right is Literal)
                with(right as Literal) {
                    assertEquals(2.0, value)
                }
            }
        }
    }
}
