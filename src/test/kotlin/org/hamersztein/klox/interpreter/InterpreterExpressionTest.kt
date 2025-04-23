package org.hamersztein.klox.interpreter

import org.hamersztein.klox.ast.expression.impl.*
import org.hamersztein.klox.ast.statement.impl.Expression
import org.hamersztein.klox.ast.statement.impl.If
import org.hamersztein.klox.ast.statement.impl.Print
import org.hamersztein.klox.ast.statement.impl.Var
import org.hamersztein.klox.environment.Environment
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType
import org.hamersztein.klox.token.TokenType.*
import org.hamersztein.klox.util.TestUtils.mockSystemOutStream
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.contracts.ExperimentalContracts
import kotlin.test.assertEquals

@ExperimentalContracts
class InterpreterExpressionTest {

    @Test
    fun `should interpret grouping expression correctly`() {
        val expression = Grouping(
            Binary(
                Literal(2.0),
                Token(STAR, "", null, 1),
                Literal(2.0)
            )
        )

        val interpreter = Interpreter()
        val result = interpreter.visitGroupingExpression(expression)

        assertEquals(4.0, result)
    }

    @Test
    fun `should interpret assign expression correctly when variable already exists in environment`() {
        val environment = Environment()
        environment.define("breakfast", "toast")

        val identifierToken = Token(IDENTIFIER, "breakfast", null, 1)

        val statements = listOf(
            Expression(
                Assign(
                    identifierToken,
                    Literal("muffin")
                )
            )
        )

        val interpreter = Interpreter(environment)
        interpreter.interpret(statements)

        assertEquals("muffin", environment[identifierToken])
    }

    @MethodSource("provideArgumentsForLiteralTest")
    @ParameterizedTest(name = "should interpret literal expression whose value is {0}")
    fun `should interpret literal expression`(value: Any?) {
        val expression = Literal(value)

        val interpreter = Interpreter()
        val result = interpreter.visitLiteralExpression(expression)

        assertEquals(expression.value, result)
    }

    @Test
    fun `should visit logical expression when operator is AND`() {
        val (outputStreamCaptor, resetOutStream) = mockSystemOutStream()

        val statements = listOf(
            If(
                Logical(
                    Literal(true),
                    Token(AND, "and", null, 1),
                    Literal(false)
                ),
                Print(Literal("true")),
                Print(Literal("false"))
            )
        )

        val interpreter = Interpreter()
        interpreter.interpret(statements)

        assertEquals("false", outputStreamCaptor.toString().trim())

        resetOutStream()
    }

    @Test
    fun `should visit logical expression when operator is OR`() {
        val (outputStreamCaptor, resetOutStream) = mockSystemOutStream()

        val statements = listOf(
            If(
                Logical(
                    Literal(true),
                    Token(OR, "and", null, 1),
                    Literal(false)
                ),
                Print(Literal("true")),
                Print(Literal("false"))
            )
        )

        val interpreter = Interpreter()
        interpreter.interpret(statements)

        assertEquals("true", outputStreamCaptor.toString().trim())

        resetOutStream()
    }

    @MethodSource("provideArgumentsForShortCircuitOrExpressionTest")
    @ParameterizedTest(name = "should short-circuit OR operation when left is truthy - {0}")
    fun `should circuit OR expression when left is truthy`(value: Any) {
        val (outputStreamCaptor, resetOutStream) = mockSystemOutStream()

        val statements = listOf(
            Print(
                Logical(
                    Literal(value),
                    Token(OR, "or", null, 1),
                    Literal(false)
                )
            )
        )

        val interpreter = Interpreter()
        interpreter.interpret(statements)

        assertEquals(value.toString(), outputStreamCaptor.toString().trim())

        resetOutStream()
    }

    @Test
    fun `should evaluate right hand OR expression when left is falsy`() {
        val (outputStreamCaptor, resetOutStream) = mockSystemOutStream()

        val statements = listOf(
            Print(
                Logical(
                    Literal(null),
                    Token(OR, "or", null, 1),
                    Literal(false)
                )
            )
        )

        val interpreter = Interpreter()
        interpreter.interpret(statements)

        assertEquals("nil", outputStreamCaptor.toString().trim())

        resetOutStream()
    }

    @ValueSource(booleans = [true, false])
    @ParameterizedTest(name = "should execute correct branch of ternary expression when condition is literal '{0}'")
    fun `should execute correct branch of ternary expression based on literal condition`(conditionValue: Boolean) {
        val environment = Environment()

        val variableNameToken = Token(IDENTIFIER, "breakfast", null, 1)

        val statements = listOf(
            Var(
                variableNameToken,
                Ternary(
                    Literal(conditionValue),
                    Literal("toast"),
                    Literal("muffin")
                )
            )
        )

        val interpreter = Interpreter(environment)
        interpreter.interpret(statements)

        assertEquals(if (conditionValue) "toast" else "muffin", environment.get(variableNameToken))
    }

    @Test
    fun `should execute correct branch of ternary expression based on expression condition`() {
        val environment = Environment()

        val variableNameToken = Token(IDENTIFIER, "breakfast", null, 1)

        val statements = listOf(
            Var(
                variableNameToken,
                Ternary(
                    Binary(Literal(true), Token(EQUAL_EQUAL, "==", null, 1), Literal(false)),
                    Literal("toast"),
                    Literal("muffin")
                )
            )
        )

        val interpreter = Interpreter(environment)
        interpreter.interpret(statements)

        assertEquals("muffin", environment.get(variableNameToken))
    }

    @MethodSource("provideArgumentsForUnaryTest")
    @ParameterizedTest(name = "should interpret unary expression and return {2} when operator token is {0} and literal is {1}")
    fun `should interpret unary expression`(tokenType: TokenType, literal: Any?, expected: Any) {
        val expression = Unary(Token(tokenType, "", null, 1), Literal(literal))

        val interpreter = Interpreter()
        val result = interpreter.visitUnaryExpression(expression)

        assertEquals(expected, result)
    }

    @MethodSource("provideArgumentsForBinaryTest")
    @ParameterizedTest(name = "should interpret binary expression and return {3} when operator is {0}, left is {1}, and right is {2}")
    fun `should interpret binary expression`(tokenType: TokenType, left: Any?, right: Any?, expected: Any) {
        val expression = Binary(
            Literal(left),
            Token(tokenType, "", null, 1),
            Literal(right)
        )

        val interpreter = Interpreter()
        val result = interpreter.visitBinaryExpression(expression)

        assertEquals(expected, result)
    }

    companion object {
        @JvmStatic
        private fun provideArgumentsForLiteralTest() = listOf(
            Arguments.of(1.0),
            Arguments.of("hello"),
            Arguments.of(null),
        )

        @JvmStatic
        private fun provideArgumentsForUnaryTest() = listOf(
            Arguments.of(MINUS, 1.0, -1.0),
            Arguments.of(BANG, false, true),
        )

        @JvmStatic
        private fun provideArgumentsForBinaryTest() = listOf(
            Arguments.of(MINUS, 1.0, 2.0, -1.0),
            Arguments.of(SLASH, 2.0, 1.0, 2.0),
            Arguments.of(STAR, 2.0, 2.0, 4.0),
            Arguments.of(PLUS, 2.0, 2.0, 4.0),
            Arguments.of(PLUS, "hello", "world", "helloworld"),
            Arguments.of(PLUS, "hello", 2.1, "hello2.1"),
            Arguments.of(PLUS, 2.1, "hello", "2.1hello"),
            Arguments.of(PLUS, "hello", 2.0, "hello2"),
            Arguments.of(PLUS, 2.0, "hello", "2hello"),
            Arguments.of(PLUS, true, "hello", "truehello"),
            Arguments.of(PLUS, "hello", true, "hellotrue"),
            Arguments.of(GREATER, 1.0, 2.0, false),
            Arguments.of(GREATER_EQUAL, 1.0, 2.0, false),
            Arguments.of(LESS, 1.0, 2.0, true),
            Arguments.of(LESS_EQUAL, 1.0, 2.0, true),
            Arguments.of(BANG_EQUAL, null, null, false),
            Arguments.of(BANG_EQUAL, null, 2.0, true),
            Arguments.of(BANG_EQUAL, 2.0, 2.0, false),
            Arguments.of(BANG_EQUAL, 2.0, 1.0, true),
            Arguments.of(BANG_EQUAL, "hello", "hello", false),
            Arguments.of(BANG_EQUAL, "hello", "world", true),
            Arguments.of(EQUAL_EQUAL, null, null, true),
            Arguments.of(EQUAL_EQUAL, null, 2.0, false),
            Arguments.of(EQUAL_EQUAL, 2.0, 2.0, true),
            Arguments.of(EQUAL_EQUAL, 2.0, 1.0, false),
            Arguments.of(EQUAL_EQUAL, "hello", "hello", true),
            Arguments.of(EQUAL_EQUAL, "hello", "world", false),
        )

        @JvmStatic
        private fun provideArgumentsForShortCircuitOrExpressionTest() = listOf(
            Arguments.of("hi"),
            Arguments.of(true),
            Arguments.of(2),
        )
    }

}
