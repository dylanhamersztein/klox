package org.hamersztein.klox.interpreter

import org.hamersztein.klox.ast.expression.impl.*
import org.hamersztein.klox.ast.statement.impl.Block
import org.hamersztein.klox.ast.statement.impl.Expression
import org.hamersztein.klox.ast.statement.impl.Print
import org.hamersztein.klox.ast.statement.impl.Var
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType
import org.hamersztein.klox.token.TokenType.*
import org.hamersztein.klox.util.TestUtils.mockSystemErrorStream
import org.hamersztein.klox.util.TestUtils.mockSystemOutStream
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.contracts.ExperimentalContracts
import kotlin.test.assertEquals

@ExperimentalContracts
class InterpreterErrorTest {

    @Test
    fun `should log runtime error when attempting to reference variable that hasn't been declared`() {
        val (outputStreamCaptor, resetSystemError) = mockSystemErrorStream()

        val statements = listOf(Print(Variable(Token(IDENTIFIER, "breakfast", null, 1))))

        val interpreter = Interpreter()

        interpreter.interpret(statements)

        assertEquals("Undefined variable breakfast\n[line 1]", outputStreamCaptor.toString().trim())

        resetSystemError()
    }

    @Test
    fun `should log error when assign expression refers to variable that does not exist`() {
        val (outputStream, resetSystemError) = mockSystemErrorStream()

        val identifierToken = Token(IDENTIFIER, "breakfast", null, 1)

        val statements = listOf(
            Expression(
                Assign(
                    identifierToken,
                    Literal("muffin")
                )
            )
        )

        val interpreter = Interpreter()

        interpreter.interpret(statements)

        assertEquals("Undefined variable breakfast\n[line 1]", outputStream.toString().trim())

        resetSystemError()
    }

    @Test
    fun `should throw RuntimeError when interpreting unary minus where operand is not a number`() {
        val token = Token(MINUS, "", null, 1)
        val expression = Unary(token, Literal("muffin"))

        val interpreter = Interpreter()

        val exception = assertThrows<RuntimeError> {
            interpreter.visitUnaryExpression(expression)
        }

        assertEquals(token, exception.token)
        assertEquals("Operand must be a number.", exception.message)
    }

    @Test
    fun `should throw error when declaring variable in block scope and referencing it outside block scope`() {
        val (outputStreamCaptor, resetOutStream) = mockSystemOutStream()
        val (errorStreamCaptor, resetErrorStream) = mockSystemErrorStream()

        val variableName = "breakfast"
        val blockScopeValue = "toast"

        val blockScopedStatements = listOf(
            Block(
                statements = listOf(
                    Var(
                        Token(IDENTIFIER, variableName, null, 1),
                        Literal(blockScopeValue)
                    ),
                    Print(
                        Variable(Token(IDENTIFIER, variableName, null, 1))
                    )
                )
            )
        )

        val interpreter = Interpreter()
        interpreter.interpret(blockScopedStatements)

        assertEquals(blockScopeValue, outputStreamCaptor.toString().trim())
        outputStreamCaptor.reset()

        val globalStatements = listOf(
            Print(
                Variable(Token(IDENTIFIER, variableName, null, 2))
            )
        )

        interpreter.interpret(globalStatements)

        assertTrue(outputStreamCaptor.toString().trim().isEmpty())
        assertEquals("Undefined variable $variableName\n[line 2]", errorStreamCaptor.toString().trim())

        resetOutStream()
        resetErrorStream()
    }

    @ParameterizedTest(name = "should throw runtime error when interpreting binary expression with operator {0}, left {1}, and right {2}")
    @MethodSource("provideArgumentsForBinaryWithBadOperandsTest")
    fun `should throw RuntimeError when interpreting binary expressions with bad operands`(
        tokenType: TokenType,
        left: Any?,
        right: Any?,
        expectedErrorMessage: String
    ) {
        val token = Token(tokenType, "", null, 1)
        val expression = Binary(Literal(left), token, Literal(right))

        val interpreter = Interpreter()

        val exception = assertThrows<RuntimeError> {
            interpreter.visitBinaryExpression(expression)
        }

        assertEquals(token, exception.token)
        assertEquals(expectedErrorMessage, exception.message)

    }

    companion object {
        @JvmStatic
        private fun provideArgumentsForBinaryWithBadOperandsTest() = listOf(
            Arguments.of(MINUS, "hello", 1.0, "Operand must be a number."),
            Arguments.of(MINUS, 1.0, "hello", "Operand must be a number."),
            Arguments.of(STAR, "hello", 1.0, "Operand must be a number."),
            Arguments.of(STAR, 1.0, "hello", "Operand must be a number."),
            Arguments.of(SLASH, "hello", 1.0, "Operand must be a number."),
            Arguments.of(SLASH, 1.0, "hello", "Operand must be a number."),
            Arguments.of(SLASH, 1.0, 0.0, "Cannot divide by 0"),
            Arguments.of(PLUS, false, true, "Could not add false to true."),
            Arguments.of(PLUS, 1.0, true, "Could not add 1 to true."),
            Arguments.of(PLUS, 1.1, true, "Could not add 1.1 to true.")
        )
    }

}
