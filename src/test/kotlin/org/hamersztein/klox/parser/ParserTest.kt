package org.hamersztein.klox.parser

import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.expression.impl.Binary
import org.hamersztein.klox.ast.expression.impl.Grouping
import org.hamersztein.klox.ast.expression.impl.Literal
import org.hamersztein.klox.ast.expression.impl.Unary
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType.*
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ParserTest {

    @MethodSource("provideArgsToExpressionInstanceTest")
    @ParameterizedTest(name = "{0}")
    fun `should create the right instance of expression`(
        _ignored: String,
        tokens: List<Token>,
        expressionClass: KClass<Expression>
    ) {
        val parser = Parser(tokens)

        val expression = parser.parse()

        assertNotNull(expression)
        assertEquals(expressionClass, expression!!::class)
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
        fun provideArgsToExpressionInstanceTest() = listOf(
            Arguments.of(
                "should create a binary expression from equality operator",
                listOf(
                    Token(NUMBER, "3", 3.0, 1),
                    Token(EQUAL_EQUAL, "==", null, 1),
                    Token(NUMBER, "2", 2.0, 1),
                    Token(EOF, "", null, 1),
                ),
                Binary::class
            ),
            Arguments.of(
                "should create a grouping expression",
                listOf(
                    Token(LEFT_PAREN, "(", null, 1),
                    Token(NUMBER, "3", 3.0, 1),
                    Token(EQUAL_EQUAL, "==", null, 1),
                    Token(NUMBER, "2", 2.0, 1),
                    Token(RIGHT_PAREN, ")", null, 1),
                    Token(EOF, "", null, 1),
                ),
                Grouping::class
            ),
            Arguments.of(
                "should create a unary expression from a negation",
                listOf(
                    Token(BANG, "!", null, 1),
                    Token(FALSE, "false", false, 1),
                    Token(EOF, "", null, 1),
                ),
                Unary::class
            ),
            Arguments.of(
                "should create a unary expression from a negation of a number",
                listOf(
                    Token(MINUS, "-", null, 1),
                    Token(NUMBER, "2", 2.0, 1),
                    Token(EOF, "", null, 1),
                ),
                Unary::class
            ),
            Arguments.of(
                "should create a literal expression from a string",
                listOf(
                    Token(STRING, "\"hello\"", "hello", 1),
                    Token(EOF, "", null, 1),
                ),
                Literal::class
            ),
            Arguments.of(
                "should create a literal expression from a number",
                listOf(
                    Token(STRING, "1", 1, 1),
                    Token(EOF, "", null, 1),
                ),
                Literal::class
            ),
            Arguments.of(
                "should create a binary expression from more than one operation",
                listOf(
                    Token(NUMBER, "1", 1.0, 1),
                    Token(PLUS, "+", null, 1),
                    Token(NUMBER, "2", 2.0, 1),
                    Token(STAR, "*", null, 1),
                    Token(NUMBER, "3", 3.0, 1),
                    Token(EOF, "", null, 1),
                ),
                Binary::class
            ),
            Arguments.of(
                "should create a binary expression from more than one grouped operation",
                listOf(
                    Token(LEFT_PAREN, "(", null, 1),
                    Token(NUMBER, "1", 1.0, 1),
                    Token(PLUS, "+", null, 1),
                    Token(NUMBER, "2", 2.0, 1),
                    Token(RIGHT_PAREN, ")", null, 1),
                    Token(STAR, "*", null, 1),
                    Token(NUMBER, "3", 3.0, 1),
                    Token(EOF, "", null, 1),
                ),
                Binary::class
            )
        )

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