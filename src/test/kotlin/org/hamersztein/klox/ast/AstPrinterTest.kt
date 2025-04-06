package org.hamersztein.klox.ast

import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.expression.impl.*
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType.MINUS
import org.hamersztein.klox.token.TokenType.STAR
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class AstPrinterTest {

    private val astPrinter = AstPrinter()

    @ParameterizedTest(name = "should print {0} expression")
    @MethodSource("provideArgumentsForPrintExpressionTest")
    fun `should print expression`(_ignored: String, expression: Expression, expected: String) {
        assertEquals(expected, astPrinter.print(expression))
    }


    companion object {
        @JvmStatic
        fun provideArgumentsForPrintExpressionTest() = listOf(
            Arguments.of("Binary", Binary(Literal(1), Token(STAR, "*", null, 1), Literal(2)), "(* 1 2)"),
            Arguments.of("Unary", Unary(Token(MINUS, "-", null, 1), Literal(3)), "(- 3)"),
            Arguments.of("Grouping", Grouping(Literal(4)), "(group 4)"),
            Arguments.of("Literal", Literal(5), "5"),
            Arguments.of("Ternary", Ternary(Literal(true), Literal(2.0), Literal(3.0)), "(true 2.0 3.0)"),
            Arguments.of(
                "Ternary (with complex condition)",
                Ternary(Binary(Literal(1), Token(STAR, "*", null, 1), Literal(2)), Literal(2.0), Literal(3.0)),
                "((* 1 2) 2.0 3.0)"
            ),
        )
    }
}