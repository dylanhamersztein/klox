package org.hamersztein.klox.ast

import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.expression.impl.*
import org.hamersztein.klox.ast.expression.impl.Set

class AstPrinter : Visitor<String> {
    fun print(expression: Expression) = expression.accept(this)

    override fun visitBinaryExpr(expr: Binary) = parenthesize(expr.operator.lexeme, expr.left, expr.right)

    override fun visitGroupingExpr(expr: Grouping) = parenthesize("group", expr.expression)

    override fun visitLiteralExpr(expr: Literal) = expr.value?.toString() ?: "nil"

    override fun visitUnaryExpr(expr: Unary) = parenthesize(expr.operator.lexeme, expr.right)

    override fun visitAssignExpr(expr: Assign): String {
        TODO("Not yet implemented")
    }

    override fun visitCallExpr(expr: Call): String {
        TODO("Not yet implemented")
    }

    override fun visitGetExpr(expr: Get): String {
        TODO("Not yet implemented")
    }

    override fun visitLogicalExpr(expr: Logical): String {
        TODO("Not yet implemented")
    }

    override fun visitSetExpr(expr: Set): String {
        TODO("Not yet implemented")
    }

    override fun visitSuperExpr(expr: Super): String {
        TODO("Not yet implemented")
    }

    override fun visitThisExpr(expr: This): String {
        TODO("Not yet implemented")
    }

    override fun visitVariableExpr(expr: Variable): String {
        TODO("Not yet implemented")
    }

    private fun parenthesize(name: String, vararg expressions: Expression) =
        StringBuilder().apply {
            append("(")
            append(name)

            expressions.forEach { expression ->
                append(" ")
                append(expression.accept(this@AstPrinter))
            }

            append(")")
        }.toString()
}