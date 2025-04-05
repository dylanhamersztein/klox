package org.hamersztein.klox.ast

import org.hamersztein.klox.ast.expression.impl.Binary
import org.hamersztein.klox.ast.expression.impl.Grouping
import org.hamersztein.klox.ast.expression.impl.Literal
import org.hamersztein.klox.ast.expression.impl.Unary
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType.MINUS
import org.hamersztein.klox.token.TokenType.STAR
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AstPrinterTest {

    private val astPrinter = AstPrinter()

    @Test
    fun `should print expression`() {
        val expression = Binary(
            Unary(
                Token(MINUS, "-", null, 1),
                Literal(123)
            ),
            Token(STAR, "*", null, 1),
            Grouping(Literal(45.67))
        )

        assertEquals("(* (- 123) (group 45.67))", astPrinter.print(expression))
    }
}