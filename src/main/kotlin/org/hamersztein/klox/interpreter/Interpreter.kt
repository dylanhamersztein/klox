package org.hamersztein.klox.interpreter

import org.hamersztein.klox.ast.Visitor
import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.expression.impl.*
import org.hamersztein.klox.ast.expression.impl.Set
import org.hamersztein.klox.token.TokenType.*

class Interpreter : Visitor<Any?> {

    override fun visitLiteralExpr(expr: Literal) = expr.value

    override fun visitGroupingExpr(expr: Grouping) = evaluate(expr.expression)

    override fun visitUnaryExpr(expr: Unary): Any? {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            MINUS -> return -(right as Double)
            BANG -> !isTruthy(right)
            else -> null
        }
    }

    override fun visitBinaryExpr(expr: Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            MINUS -> (left as Double) - (right as Double)
            SLASH -> (left as Double) / (right as Double)
            STAR -> (left as Double) * (right as Double)
            PLUS -> when {
                left is Double && right is Double -> left + right
                left is String && right is String -> left + right
                else -> null
            }

            GREATER -> (left as Double) > (right as Double)
            GREATER_EQUAL -> (left as Double) >= (right as Double)
            LESS -> (left as Double) < (right as Double)
            LESS_EQUAL -> (left as Double) <= (right as Double)
            BANG_EQUAL -> !isEqual(left, right)
            EQUAL_EQUAL -> isEqual(left, right)

            else -> null
        }
    }

    override fun visitAssignExpr(expr: Assign): Any? {
        TODO("Not yet implemented")
    }

    override fun visitCallExpr(expr: Call): Any? {
        TODO("Not yet implemented")
    }

    override fun visitGetExpr(expr: Get): Any? {
        TODO("Not yet implemented")
    }

    override fun visitLogicalExpr(expr: Logical): Any? {
        TODO("Not yet implemented")
    }

    override fun visitSetExpr(expr: Set): Any? {
        TODO("Not yet implemented")
    }

    override fun visitSuperExpr(expr: Super): Any? {
        TODO("Not yet implemented")
    }

    override fun visitThisExpr(expr: This): Any? {
        TODO("Not yet implemented")
    }

    override fun visitVariableExpr(expr: Variable): Any? {
        TODO("Not yet implemented")
    }

    override fun visitTernaryExpression(expr: Ternary): Any? {
        TODO("Not yet implemented")
    }

    private fun evaluate(expr: Expression) = expr.accept(this)

    private fun isTruthy(value: Any?) = when (value) {
        null -> false
        is Boolean -> value
        else -> true
    }

    private fun isEqual(a: Any?, b: Any?) = when {
        a == null && b == null -> true
        a == null -> false
        else -> a == b
    }
}