package org.hamersztein.klox.parser

import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.expression.impl.*
import org.hamersztein.klox.ast.statement.impl.*
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType.*
import org.hamersztein.klox.util.TestUtils.mockSystemErrorStream
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
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
        val (outputStreamCaptor, resetSystemError) = mockSystemErrorStream()

        val tokens = listOf(
            Token(LEFT_PAREN, "(", null, 1),
            Token(NUMBER, "1", 1.0, 1),
            Token(PLUS, "+", null, 1),
            Token(NUMBER, "1", 1.0, 1),
            Token(EOF, "", null, 1),
        )

        Parser(tokens).parse()

        assertEquals("[1]: Error at end: Expect ')' after expression.", outputStreamCaptor.toString().trim())

        resetSystemError()
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

    @Test
    fun `should create an assignment expression`() {
        val identifierToken = Token(IDENTIFIER, "muffin", null, 1)

        val tokens = listOf(
            identifierToken,
            Token(EQUAL, "=", null, 1),
            Token(NUMBER, "1", 1.0, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1)
        )

        assertTokensThatProduceExpressionStatement(tokens) {
            assertTrue(it is Assign)
            with(it as Assign) {
                assertEquals(identifierToken, name)
                assertEquals(Literal(1.0), value)
            }
        }
    }

    @Test
    fun `should log error on invalid assignment target`() {
        val (outputStreamCaptor, resetSystemError) = mockSystemErrorStream()

        val tokens = listOf(
            Token(NUMBER, "1", 1.0, 1),
            Token(EQUAL, "=", null, 1),
            Token(NUMBER, "1", 1.0, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1)
        )

        val parser = Parser(tokens)
        parser.parse()

        assertEquals("[1]: Error  at '=': Invalid assignment target.", outputStreamCaptor.toString().trim())

        resetSystemError()
    }

    @Test
    fun `should create block statement`() {
        val tokens = listOf(
            Token(LEFT_BRACE, "{", null, 1),
            Token(VAR, "var", null, 2),
            Token(IDENTIFIER, "muffin", null, 2),
            Token(EQUAL, "=", null, 2),
            Token(NUMBER, "1", 1.0, 2),
            Token(SEMICOLON, ";", null, 2),
            Token(RIGHT_BRACE, "}", null, 3),
            Token(EOF, "", null, 3),
        )

        val parser = Parser(tokens)
        val statements = parser.parse()

        assertEquals(1, statements.size)
        assertTrue(statements[0] is Block)
        assertEquals(1, (statements[0] as Block).statements.size)
    }

    @Test
    fun `should create an if statement`() {
        val tokens = listOf(
            Token(IF, "if", null, 1),
            Token(LEFT_PAREN, "(", null, 1),
            Token(TRUE, "true", 1.0, 1),
            Token(RIGHT_PAREN, ")", null, 1),
            Token(IDENTIFIER, "muffin", null, 2),
            Token(EQUAL, "=", null, 2),
            Token(NUMBER, "1", 1.0, 2),
            Token(SEMICOLON, ";", null, 2),
            Token(EOF, "", null, 3),
        )

        val parser = Parser(tokens)
        val statements = parser.parse()

        assertEquals(1, statements.size)
        assertTrue(statements[0] is If)

        with(statements[0] as If) {
            assertTrue(condition is Literal)
            assertTrue(thenBranch is ExpressionStatement)
            assertNull(elseBranch)
        }
    }

    @Test
    fun `should create an if statement with an else block`() {
        val tokens = listOf(
            Token(IF, "if", null, 1),
            Token(LEFT_PAREN, "(", null, 1),
            Token(TRUE, "true", 1.0, 1),
            Token(RIGHT_PAREN, ")", null, 1),
            Token(IDENTIFIER, "muffin", null, 2),
            Token(EQUAL, "=", null, 2),
            Token(NUMBER, "1", 1.0, 2),
            Token(SEMICOLON, ";", null, 2),
            Token(ELSE, "else", null, 3),
            Token(IDENTIFIER, "muffin", null, 3),
            Token(EQUAL, "=", null, 3),
            Token(NUMBER, "2", 2.0, 3),
            Token(SEMICOLON, ";", null, 3),
            Token(EOF, "", null, 3)
        )

        val parser = Parser(tokens)
        val statements = parser.parse()

        assertEquals(1, statements.size)
        assertTrue(statements[0] is If)

        with(statements[0] as If) {
            assertTrue(condition is Literal)
            assertTrue(thenBranch is ExpressionStatement)
            assertTrue(elseBranch is ExpressionStatement)
        }
    }

    @Test
    fun `should create while statement`() {
        val tokens = listOf(
            Token(WHILE, "while", null, 1),
            Token(LEFT_PAREN, "(", null, 1),
            Token(TRUE, "true", true, 1),
            Token(RIGHT_PAREN, ")", null, 1),
            Token(PRINT, "print", null, 1),
            Token(STRING, "\"hello\"", "hello", 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        val parser = Parser(tokens)
        val statements = parser.parse()

        assertEquals(1, statements.size)
        assertTrue(statements[0] is While)

        with(statements[0] as While) {
            assertTrue(condition is Literal)
            assertTrue(body is Print)
        }
    }

    @Test
    fun `should error when while statement condition does not have left parenthesis`() {
        val (errorStreamCaptor, resetSystemError) = mockSystemErrorStream()

        val tokens = listOf(
            Token(WHILE, "while", null, 1),
            Token(TRUE, "true", true, 1),
            Token(RIGHT_PAREN, ")", null, 1),
            Token(PRINT, "print", null, 1),
            Token(STRING, "\"hello\"", "hello", 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        val parser = Parser(tokens)
        parser.parse()

        assertEquals("[1]: Error  at 'true': Expect '(' after 'while'.", errorStreamCaptor.toString().trim())

        resetSystemError()
    }

    @Test
    fun `should error when while statement condition does not have right parenthesis`() {
        val (errorStreamCaptor, resetSystemError) = mockSystemErrorStream()

        val tokens = listOf(
            Token(WHILE, "while", null, 1),
            Token(LEFT_PAREN, "(", null, 1),
            Token(TRUE, "true", true, 1),
            Token(PRINT, "print", null, 1),
            Token(STRING, "\"hello\"", "hello", 1),
            Token(SEMICOLON, ";", null, 1),
            Token(EOF, "", null, 1),
        )

        val parser = Parser(tokens)
        parser.parse()

        assertEquals("[1]: Error  at 'print': Expect ')' after while condition.", errorStreamCaptor.toString().trim())

        resetSystemError()
    }

    @Test
    fun `should create a for loop`() {
        val tokens = listOf(
            Token(FOR, "for", null, 1),
            Token(LEFT_PAREN, "(", null, 1),
            Token(VAR, "var", null, 1),
            Token(IDENTIFIER, "i", null, 1),
            Token(EQUAL, "=", null, 1),
            Token(NUMBER, "0", 0, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(IDENTIFIER, "i", null, 1),
            Token(LESS, "<", null, 1),
            Token(NUMBER, "1000", 1000, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(IDENTIFIER, "i", null, 1),
            Token(EQUAL, "=", null, 1),
            Token(IDENTIFIER, "i", null, 1),
            Token(PLUS, "+", null, 1),
            Token(NUMBER, "1", 1, 1),
            Token(RIGHT_PAREN, ")", null, 1),
            Token(LEFT_BRACE, "{", null, 1),
            Token(PRINT, "print", null, 2),
            Token(IDENTIFIER, "i", null, 2),
            Token(SEMICOLON, ";", null, 2),
            Token(RIGHT_BRACE, "}", null, 3),
            Token(EOF, "", null, 4)
        )

        val parser = Parser(tokens)
        val statements = parser.parse()

        assertEquals(1, statements.size)
        assertTrue(statements[0] is Block)
        with(statements[0] as Block) {
            assertEquals(2, this.statements.size)

            assertTrue(this.statements[0] is Var)
            with(this.statements[0] as Var) {
                assertEquals(Token(IDENTIFIER, "i", null, 1), this.name)
                assertNotNull(this.initializer)
                assertEquals(Literal(0), this.initializer)
            }

            assertTrue(this.statements[1] is While)
            with(this.statements[1] as While) {
                assertTrue(this.condition is Binary)
                with(this.condition as Binary) {
                    assertEquals(Variable(Token(IDENTIFIER, "i", null, 1)), this.left)
                    assertEquals(Token(LESS, "<", null, 1), this.operator)
                    assertEquals(Variable(Token(IDENTIFIER, "i", null, 1)), this.left)
                    assertEquals(Literal(1000), this.right)
                }

                assertTrue(this.body is Block)
                with(this.body as Block) {
                    assertEquals(2, this.statements.size)

                    assertTrue(this.statements[0] is Block)
                    with(this.statements[0] as Block) {
                        assertEquals(1, this.statements.size)
                        assertTrue(this.statements[0] is Print)
                    }

                    assertTrue(this.statements[1] is ExpressionStatement)
                    with(this.statements[1] as ExpressionStatement) {
                        assertTrue(this.expression is Assign)
                    }
                }
            }
        }
    }

    @Test
    fun `should create a for loop with without an initializer`() {
        val tokens = listOf(
            Token(FOR, "for", null, 1),
            Token(LEFT_PAREN, "(", null, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(IDENTIFIER, "i", null, 1),
            Token(LESS, "<", null, 1),
            Token(NUMBER, "1000", 1000, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(IDENTIFIER, "i", null, 1),
            Token(EQUAL, "=", null, 1),
            Token(IDENTIFIER, "i", null, 1),
            Token(PLUS, "+", null, 1),
            Token(NUMBER, "1", 1, 1),
            Token(RIGHT_PAREN, ")", null, 1),
            Token(LEFT_BRACE, "{", null, 1),
            Token(PRINT, "print", null, 2),
            Token(IDENTIFIER, "i", null, 2),
            Token(SEMICOLON, ";", null, 2),
            Token(RIGHT_BRACE, "}", null, 3),
            Token(EOF, "", null, 4)
        )

        val parser = Parser(tokens)
        val statements = parser.parse()

        assertEquals(1, statements.size)
        assertTrue(statements[0] is While)

        with(statements[0] as While) {
            assertTrue(this.condition is Binary)
            with(this.condition as Binary) {
                assertEquals(Variable(Token(IDENTIFIER, "i", null, 1)), this.left)
                assertEquals(Token(LESS, "<", null, 1), this.operator)
                assertEquals(Variable(Token(IDENTIFIER, "i", null, 1)), this.left)
                assertEquals(Literal(1000), this.right)
            }

            assertTrue(this.body is Block)
            with(this.body as Block) {
                assertEquals(2, this.statements.size)

                assertTrue(this.statements[0] is Block)
                with(this.statements[0] as Block) {
                    assertEquals(1, this.statements.size)
                    assertTrue(this.statements[0] is Print)
                }

                assertTrue(this.statements[1] is ExpressionStatement)
                with(this.statements[1] as ExpressionStatement) {
                    assertTrue(this.expression is Assign)
                }
            }
        }
    }

    @Test
    fun `should create a for loop with the condition is true if one is not supplied`() {
        val tokens = listOf(
            Token(FOR, "for", null, 1),
            Token(LEFT_PAREN, "(", null, 1),
            Token(VAR, "var", null, 1),
            Token(IDENTIFIER, "i", null, 1),
            Token(EQUAL, "=", null, 1),
            Token(NUMBER, "0", 0, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(IDENTIFIER, "i", null, 1),
            Token(EQUAL, "=", null, 1),
            Token(IDENTIFIER, "i", null, 1),
            Token(PLUS, "+", null, 1),
            Token(NUMBER, "1", 1, 1),
            Token(RIGHT_PAREN, ")", null, 1),
            Token(LEFT_BRACE, "{", null, 1),
            Token(PRINT, "print", null, 2),
            Token(IDENTIFIER, "i", null, 2),
            Token(SEMICOLON, ";", null, 2),
            Token(RIGHT_BRACE, "}", null, 3),
            Token(EOF, "", null, 4)
        )

        val parser = Parser(tokens)
        val statements = parser.parse()

        assertEquals(1, statements.size)
        assertTrue(statements[0] is Block)
        with(statements[0] as Block) {
            assertEquals(2, this.statements.size)

            assertTrue(this.statements[0] is Var)
            with(this.statements[0] as Var) {
                assertEquals(Token(IDENTIFIER, "i", null, 1), this.name)
                assertNotNull(this.initializer)
                assertEquals(Literal(0), this.initializer)
            }

            assertTrue(this.statements[1] is While)
            with(this.statements[1] as While) {
                assertEquals(Literal(true), this.condition)

                assertTrue(this.body is Block)
                with(this.body as Block) {
                    assertEquals(2, this.statements.size)

                    assertTrue(this.statements[0] is Block)
                    with(this.statements[0] as Block) {
                        assertEquals(1, this.statements.size)
                        assertTrue(this.statements[0] is Print)
                    }

                    assertTrue(this.statements[1] is ExpressionStatement)
                    with(this.statements[1] as ExpressionStatement) {
                        assertTrue(this.expression is Assign)
                    }
                }
            }
        }
    }

    @Test
    fun `should create a for loop without an increment`() {
        val tokens = listOf(
            Token(FOR, "for", null, 1),
            Token(LEFT_PAREN, "(", null, 1),
            Token(VAR, "var", null, 1),
            Token(IDENTIFIER, "i", null, 1),
            Token(EQUAL, "=", null, 1),
            Token(NUMBER, "0", 0, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(IDENTIFIER, "i", null, 1),
            Token(LESS, "<", null, 1),
            Token(NUMBER, "1000", 1000, 1),
            Token(SEMICOLON, ";", null, 1),
            Token(RIGHT_PAREN, ")", null, 1),
            Token(LEFT_BRACE, "{", null, 1),
            Token(PRINT, "print", null, 2),
            Token(IDENTIFIER, "i", null, 2),
            Token(SEMICOLON, ";", null, 2),
            Token(RIGHT_BRACE, "}", null, 3),
            Token(EOF, "", null, 4)
        )

        val parser = Parser(tokens)
        val statements = parser.parse()

        assertEquals(1, statements.size)
        assertTrue(statements[0] is Block)
        with(statements[0] as Block) {
            assertEquals(2, this.statements.size)

            assertTrue(this.statements[0] is Var)
            with(this.statements[0] as Var) {
                assertEquals(Token(IDENTIFIER, "i", null, 1), this.name)
                assertNotNull(this.initializer)
                assertEquals(Literal(0), this.initializer)
            }

            assertTrue(this.statements[1] is While)
            with(this.statements[1] as While) {
                assertTrue(this.condition is Binary)
                with(this.condition as Binary) {
                    assertEquals(Variable(Token(IDENTIFIER, "i", null, 1)), this.left)
                    assertEquals(Token(LESS, "<", null, 1), this.operator)
                    assertEquals(Variable(Token(IDENTIFIER, "i", null, 1)), this.left)
                    assertEquals(Literal(1000), this.right)
                }

                assertTrue(this.body is Block)
                with(this.body as Block) {
                    assertEquals(1, this.statements.size)
                    assertTrue(this.statements[0] is Print)
                }
            }
        }
    }

    private fun assertTokensThatProduceExpressionStatement(
        tokens: List<Token>,
        assertFunction: (e: Expression) -> Unit
    ) {
        val parser = Parser(tokens)

        val statements = parser.parse()

        assertNotNull(statements)
        assertEquals(1, statements.size)
        assertTrue(statements[0] is ExpressionStatement)

        assertFunction((statements[0] as ExpressionStatement).expression)
    }

}