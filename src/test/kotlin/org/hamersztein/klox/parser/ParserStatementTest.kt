package org.hamersztein.klox.parser

import org.hamersztein.klox.ast.expression.impl.Binary
import org.hamersztein.klox.ast.expression.impl.Literal
import org.hamersztein.klox.ast.statement.impl.Block
import org.hamersztein.klox.ast.statement.impl.If
import org.hamersztein.klox.ast.statement.impl.Print
import org.hamersztein.klox.ast.statement.impl.Var
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType.*
import org.hamersztein.klox.util.TestUtils.mockSystemErrorStream
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts
import kotlin.test.assertEquals
import org.hamersztein.klox.ast.statement.impl.Expression as ExpressionStatement

@ExperimentalContracts
class ParserStatementTest {

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

}
