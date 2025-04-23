package org.hamersztein.klox.parser

import org.hamersztein.klox.ast.expression.impl.Binary
import org.hamersztein.klox.ast.expression.impl.Grouping
import org.hamersztein.klox.ast.expression.impl.Literal
import org.hamersztein.klox.parser.ParserTestUtil.assertTokensThatProduceExpressionStatement
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts
import kotlin.test.assertEquals

@ExperimentalContracts
class ParserBinaryTest {

    @Test
    fun `should create binary expression`() {
        val tokens = listOf(
            Token(NUMBER, "3", 3.0, 1),
            Token(EQUAL_EQUAL, "==", null, 1),
            Token(NUMBER, "2", 2.0, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        assertTokensThatProduceExpressionStatement(tokens) {
            assertTrue(it is Binary)
            with(it as Binary) {
                assertTrue(left is Literal)
                with(left as Literal) {
                    assertEquals(3.0, value)
                }

                assertEquals(EQUAL_EQUAL, operator.type)

                assertTrue(right is Literal)
                with(right as Literal) {
                    assertEquals(2.0, value)
                }
            }
        }
    }

    @Test
    fun `should create a binary expression from more than one operation`() {
        val tokens = listOf(
            Token(NUMBER, "1", 1.0, 1),
            Token(PLUS, "+", null, 1),
            Token(NUMBER, "2", 2.0, 1),
            Token(STAR, "*", null, 1),
            Token(NUMBER, "3", 3.0, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        assertTokensThatProduceExpressionStatement(tokens) {
            assertTrue(it is Binary)

            with(it as Binary) {
                assertTrue(left is Literal)
                with(left as Literal) {
                    assertEquals(1.0, value)
                }

                assertEquals(PLUS, operator.type)

                assertTrue(right is Binary)
                with(right as Binary) {
                    assertTrue(left is Literal)
                    with(left as Literal) {
                        assertEquals(2.0, value)
                    }

                    assertEquals(STAR, operator.type)

                    assertTrue(right is Literal)
                    with(right as Literal) {
                        assertEquals(3.0, value)
                    }
                }
            }
        }
    }

    @Test
    fun `should create a binary expression from more than one grouped operation`() {
        val tokens = listOf(
            Token(LEFT_PAREN, "(", null, 1),
            Token(NUMBER, "1", 1.0, 1),
            Token(PLUS, "+", null, 1),
            Token(NUMBER, "2", 2.0, 1),
            Token(RIGHT_PAREN, ")", null, 1),
            Token(STAR, "*", null, 1),
            Token(NUMBER, "3", 3.0, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        assertTokensThatProduceExpressionStatement(tokens) {
            assertTrue(it is Binary)

            with(it as Binary) {
                assertTrue(left is Grouping)
                with(left as Grouping) {
                    assertTrue(expression is Binary)
                    with(expression as Binary) {
                        assertTrue(left is Literal)
                        with(left as Literal) {
                            assertEquals(1.0, value)
                        }

                        assertEquals(PLUS, operator.type)

                        assertTrue(right is Literal)
                        with(right as Literal) {
                            assertEquals(2.0, value)
                        }
                    }
                }

                assertEquals(STAR, operator.type)

                assertTrue(right is Literal)
                with(right as Literal) {
                    assertEquals(3.0, value)
                }
            }
        }
    }

}
