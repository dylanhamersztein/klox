package org.hamersztein.klox.parser

import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.token.Token
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.contracts.ExperimentalContracts
import kotlin.test.assertEquals
import org.hamersztein.klox.ast.statement.impl.Expression as ExpressionStatement

@ExperimentalContracts
object ParserTestUtil {

    fun assertTokensThatProduceExpressionStatement(
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
