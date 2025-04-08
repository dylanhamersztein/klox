package org.hamersztein.klox.ast

import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.expression.Visitor
import org.hamersztein.klox.ast.expression.impl.*
import org.hamersztein.klox.ast.expression.impl.Set

class AstPrinter : Visitor<String> {
    fun print(expression: Expression) = expression.accept(this)

    override fun visitBinaryExpression(expr: Binary) = parenthesize(expr.operator.lexeme, expr.left, expr.right)

    override fun visitGroupingExpression(expr: Grouping) = parenthesize("group", expr.expression)

    override fun visitLiteralExpression(expr: Literal) = expr.value?.toString() ?: "nil"

    override fun visitUnaryExpression(expr: Unary) = parenthesize(expr.operator.lexeme, expr.right)

    override fun visitTernaryExpression(expr: Ternary) = parenthesize(print(expr.condition), expr.left, expr.right)

    override fun visitAssignExpression(expr: Assign): String {
        TODO("Not yet implemented")
    }

    override fun visitCallExpression(expr: Call): String {
        TODO("Not yet implemented")
    }

    override fun visitGetExpression(expr: Get): String {
        TODO("Not yet implemented")
    }

    override fun visitLogicalExpression(expr: Logical): String {
        TODO("Not yet implemented")
    }

    override fun visitSetExpression(expr: Set): String {
        TODO("Not yet implemented")
    }

    override fun visitSuperExpression(expr: Super): String {
        TODO("Not yet implemented")
    }

    override fun visitThisExpression(expr: This): String {
        TODO("Not yet implemented")
    }

    override fun visitVariableExpression(expr: Variable): String {
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