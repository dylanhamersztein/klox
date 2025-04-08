package org.hamersztein.klox.parser

import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.expression.impl.*
import org.hamersztein.klox.ast.statement.impl.Print
import org.hamersztein.klox.ast.statement.impl.Var
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.contracts.ExperimentalContracts
import kotlin.test.assertEquals
import org.hamersztein.klox.ast.statement.impl.Expression as ExpressionStatement

@ExperimentalContracts
class ParserTest {

    @Test
    fun `should create a variable statement without an initializer`() {
        val tokens = listOf(
            Token(VAR, "var", null, 1),
            Token(IDENTIFIER, "breakfast", null, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        val parser = Parser(tokens)

        val statements = parser.parse()

        assertEquals(1, statements.size)
        assertTrue(statements[0] is Var)
        assertEquals("breakfast", (statements[0] as Var).name.lexeme)
        assertNull((statements[0] as Var).initializer)
    }

    @Test
    fun `should create a variable statement with a literal initializer`() {
        val tokens = listOf(
            Token(VAR, "var", null, 1),
            Token(IDENTIFIER, "breakfast", null, 1),
            Token(EQUAL, "=", null, 1),
            Token(NUMBER, "1", 1.0, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        val parser = Parser(tokens)

        val statements = parser.parse()

        assertEquals(1, statements.size)
        assertTrue(statements[0] is Var)
        assertEquals("breakfast", (statements[0] as Var).name.lexeme)
        assertNotNull((statements[0] as Var).initializer)
        assertTrue((statements[0] as Var).initializer is Literal)
    }

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
    fun `should create ternary expression where condition is a binary expression`() {
        val tokens = listOf(
            Token(NUMBER, "3", 3.0, 1),
            Token(EQUAL_EQUAL, "==", null, 1),
            Token(NUMBER, "2", 2.0, 1),
            Token(QUESTION_MARK, "?", null, 1),
            Token(NUMBER, "2", 2.0, 1),
            Token(COLON, ":", null, 1),
            Token(NUMBER, "3", 3.0, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        assertTokensThatProduceExpressionStatement(tokens) {
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
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        assertTokensThatProduceExpressionStatement(tokens) {
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

    @Test
    fun `should create variable expression from an identifier`() {
        val identifierToken = Token(IDENTIFIER, "muffin", null, 1)

        val tokens = listOf(
            identifierToken,
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        assertTokensThatProduceExpressionStatement(tokens) {
            assertTrue(it is Variable)
            with(it as Variable) {
                assertEquals(identifierToken, name)
            }
        }
    }

    @Test
    fun `should log error when parentheses aren't closed`() {
        val originalSystemErr = System.err
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setErr(PrintStream(outputStreamCaptor))

        val tokens = listOf(
            Token(LEFT_PAREN, "(", null, 1),
            Token(NUMBER, "1", 1.0, 1),
            Token(PLUS, "+", null, 1),
            Token(NUMBER, "1", 1.0, 1),
            Token(EOF, "", null, 1),
        )

        Parser(tokens).parse()

        assertEquals("[1]: Error at end: Expect ')' after expression.", outputStreamCaptor.toString().trim())

        System.setErr(originalSystemErr)
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

    @Test
    fun `should create a print statement`() {
        val tokens = listOf(
            Token(PRINT, "print", null, 1),
            Token(STRING, "\"something\"", "something", 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        val parser = Parser(tokens)

        val statements = parser.parse()

        assertEquals(1, statements.size)
        assertTrue(statements[0] is Print)
        assertTrue((statements[0] as Print).expression is Literal)
    }

    @Test
    fun `should create an expression statement`() {
        val tokens = listOf(
            Token(STRING, "\"something\"", "something", 1),
            Token(PLUS, "+", null, 1),
            Token(STRING, "\"something\"", "something", 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        val parser = Parser(tokens)

        val statements = parser.parse()

        assertEquals(1, statements.size)
        assertTrue(statements[0] is ExpressionStatement)
        assertTrue((statements[0] as ExpressionStatement).expression is Binary)
    }

    private fun assertTokensThatProduceExpressionStatement(
        tokens: List<Token>,
        assertFunction: (e: Expression) -> Unit
    ) {
        val parser = Parser(tokens)

        val expression = parser.parse()

        assertNotNull(expression)
        assertEquals(1, expression.size)
        assertTrue(expression[0] is ExpressionStatement)

        assertFunction((expression[0] as ExpressionStatement).expression)
    }

}