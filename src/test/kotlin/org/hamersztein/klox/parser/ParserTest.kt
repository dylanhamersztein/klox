package org.hamersztein.klox.parser

import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.expression.impl.*
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType.*
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ParserTest {

    @Test
    fun `should create binary expression`() {
        val tokens = listOf(
            Token(NUMBER, "3", 3.0, 1),
            Token(EQUAL_EQUAL, "==", null, 1),
            Token(NUMBER, "2", 2.0, 1),
            Token(EOF, "", null, 1),
        )

        assertTokens(tokens) {
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
    fun `should create ternary expression where condition is a binary expression`() {
        val tokens = listOf(
            Token(NUMBER, "3", 3.0, 1),
            Token(EQUAL_EQUAL, "==", null, 1),
            Token(NUMBER, "2", 2.0, 1),
            Token(QUESTION_MARK, "?", null, 1),
            Token(NUMBER, "2", 2.0, 1),
            Token(COLON, ":", null, 1),
            Token(NUMBER, "3", 3.0, 1),
            Token(EOF, "", null, 1),
        )

        assertTokens(tokens) {
            assertTrue(it is Ternary)
            with(it as Ternary) {
                assertTrue(condition is Binary)
                with(condition as Binary) {
                    assertEquals(3.0, (left as Literal).value)
                    assertEquals(EQUAL_EQUAL, operator.type)
                    assertEquals(2.0, (right as Literal).value)
                }

                assertTrue(left is Literal)
                with(left as Literal) {
                    assertEquals(2.0, value)
                }

                assertTrue(right is Literal)
                with(right as Literal) {
                    assertEquals(3.0, value)
                }
            }
        }
    }

    @Test
    fun `should create grouping expression`() {
        val tokens = listOf(
            Token(LEFT_PAREN, "(", null, 1),
            Token(NUMBER, "3", 3.0, 1),
            Token(EQUAL_EQUAL, "==", null, 1),
            Token(NUMBER, "2", 2.0, 1),
            Token(RIGHT_PAREN, ")", null, 1),
            Token(EOF, "", null, 1),
        )

        assertTokens(tokens) {
            assertTrue(it is Grouping)
            with(it as Grouping) {
                assertTrue(this.expression is Binary)
                with(this.expression as Binary) {
                    assertEquals(3.0, (left as Literal).value)
                    assertEquals(EQUAL_EQUAL, operator.type)
                    assertEquals(2.0, (right as Literal).value)
                }
            }

        }
    }

    @Test
    fun `should create a unary expression from a negation of a boolean`() {
        val tokens = listOf(
            Token(BANG, "!", null, 1),
            Token(FALSE, "false", false, 1),
            Token(EOF, "", null, 1),
        )

        assertTokens(tokens) {
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
            Token(EOF, "", null, 1),
        )

        assertTokens(tokens) {
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

    @Test
    fun `should create a literal expression from a string`() {
        val tokens = listOf(
            Token(STRING, "\"hello\"", "hello", 1),
            Token(EOF, "", null, 1),
        )

        assertTokens(tokens) {
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
            Token(EOF, "", null, 1),
        )

        assertTokens(tokens) {
            assertTrue(it is Literal)
            with(it as Literal) {
                assertEquals(1, value)
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
            Token(EOF, "", null, 1),
        )

        assertTokens(tokens) {
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
            Token(EOF, "", null, 1),
        )

        assertTokens(tokens) {
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

    private fun assertTokens(tokens: List<Token>, assertFunction: (e: Expression) -> Unit) {
        val parser = Parser(tokens)

        val expression = parser.parse()

        assertNotNull(expression)
        assertFunction(expression!!)
    }

    @ParameterizedTest
    @MethodSource("provideArgsToInvalidExpressionTest")
    fun `should return null when tokens do not make an expression`(invalidTokens: List<Token>) {
        val parser = Parser(invalidTokens)

        val expression = parser.parse()

        assertNull(expression)
    }

    companion object {
        @JvmStatic
        fun provideArgsToInvalidExpressionTest() = listOf(
            Arguments.of(
                listOf(
                    Token(LEFT_PAREN, "(", null, 1),
                    Token(EOF, "", null, 1),
                )
            ),
            Arguments.of(listOf(Token(EOF, "", null, 1))),
            Arguments.of(
                listOf(
                    Token(NUMBER, "1", 1.0, 1),
                    Token(PLUS, "+", null, 1),
                    Token(EOF, "", null, 1),
                )
            ),
            Arguments.of(
                listOf(
                    Token(AND, "and", null, 1),
                    Token(TRUE, "true", true, 1),
                    Token(EOF, "", null, 1),
                )
            )
        )
    }

}