package org.hamersztein.klox.parser

import org.hamersztein.klox.ast.expression.impl.Assign
import org.hamersztein.klox.ast.expression.impl.Binary
import org.hamersztein.klox.ast.expression.impl.Literal
import org.hamersztein.klox.ast.expression.impl.Variable
import org.hamersztein.klox.ast.statement.impl.*
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType.*
import org.hamersztein.klox.util.TestUtils.mockSystemErrorStream
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts
import kotlin.test.assertEquals

@ExperimentalContracts
class ParserLoopTest {

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

                    assertTrue(this.statements[1] is Expression)
                    with(this.statements[1] as Expression) {
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

                assertTrue(this.statements[1] is Expression)
                with(this.statements[1] as Expression) {
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

                    assertTrue(this.statements[1] is Expression)
                    with(this.statements[1] as Expression) {
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

}
