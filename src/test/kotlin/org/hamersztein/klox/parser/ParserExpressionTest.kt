package org.hamersztein.klox.parser

import org.hamersztein.klox.ast.expression.impl.*
import org.hamersztein.klox.parser.ParserTestUtil.assertTokensThatProduceExpressionStatement
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType.*
import org.hamersztein.klox.util.TestUtils.mockSystemErrorStream
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts
import kotlin.test.assertEquals

@ExperimentalContracts
class ParserExpressionTest {

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
            assertTrue(it is org.hamersztein.klox.ast.expression.impl.Grouping)
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
    fun `should log error when parentheses aren't closed in expression`() {
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

}
