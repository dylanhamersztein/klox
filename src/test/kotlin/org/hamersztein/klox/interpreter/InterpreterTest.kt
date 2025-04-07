package org.hamersztein.klox.interpreter

import org.hamersztein.klox.ast.expression.impl.Binary
import org.hamersztein.klox.ast.expression.impl.Grouping
import org.hamersztein.klox.ast.expression.impl.Literal
import org.hamersztein.klox.ast.expression.impl.Unary
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType
import org.hamersztein.klox.token.TokenType.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

class InterpreterTest {

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
        val result = interpreter.visitGroupingExpr(expression)

        assertEquals(4.0, result)
    }

    @MethodSource("provideArgumentsForLiteralTest")
    @ParameterizedTest(name = "should interpret literal expression whose value is {0}")
    fun `should interpret literal expression`(value: Any?) {
        val expression = Literal(value)

        val interpreter = Interpreter()
        val result = interpreter.visitLiteralExpr(expression)

        assertEquals(expression.value, result)
    }

    @MethodSource("provideArgumentsForUnaryTest")
    @ParameterizedTest(name = "should interpret unary expression and return {2} when operator token is {0} and literal is {1}")
    fun `should interpret unary expression`(tokenType: TokenType, literal: Any?, expected: Any) {
        val expression = Unary(Token(tokenType, "", null, 1), Literal(literal))

        val interpreter = Interpreter()
        val result = interpreter.visitUnaryExpr(expression)

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
        val result = interpreter.visitBinaryExpr(expression)

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
    }

}