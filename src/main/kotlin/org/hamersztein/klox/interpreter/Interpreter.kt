package org.hamersztein.klox.interpreter

import org.hamersztein.klox.Lox
import org.hamersztein.klox.ast.Visitor
import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.expression.impl.*
import org.hamersztein.klox.ast.expression.impl.Set
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType.*

class Interpreter : Visitor<Any?> {

    fun interpret(expression: Expression) {
        try {
            val value = evaluate(expression)
            println(stringify(value))
        } catch (e: RuntimeError) {
            Lox.runtimeError(e)
        }
    }

    override fun visitLiteralExpr(expr: Literal) = expr.value

    override fun visitGroupingExpr(expr: Grouping) = evaluate(expr.expression)

    override fun visitUnaryExpr(expr: Unary): Any? {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            MINUS -> {
                checkNumberOperand(expr.operator, right)
                -(right as Double)
            }

            BANG -> !isTruthy(right)
            else -> null
        }
    }

    override fun visitBinaryExpr(expr: Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) - (right as Double)
            }

            SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                checkRightIsNotZero(expr.operator, right)
                (left as Double) / (right as Double)
            }

            STAR -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) * (right as Double)
            }

            PLUS -> when {
                left is Double && right is Double -> left + right
                left is String || right is String -> left.toString() + right
                else -> throw RuntimeError(expr.operator, "Could not add $left to $right.")
            }

            GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) > (right as Double)
            }

            GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) >= (right as Double)
            }

            LESS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) < (right as Double)
            }

            LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) <= (right as Double)
            }

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

    private fun checkNumberOperands(operator: Token, vararg operands: Any?) {
        operands.forEach {
            checkNumberOperand(operator, it)
        }
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand !is Double) {
            throw RuntimeError(operator, "Operand must be a number.")
        }
    }

    private fun checkRightIsNotZero(token: Token, right: Any?) {
        if (right is Double && right == 0.0) {
            throw RuntimeError(token, "Cannot divide by 0")
        }
    }

    private fun stringify(value: Any?) = when (value) {
        null -> "nil"

        is Double -> {
            var text = value.toString()

            if (text.endsWith(".0")) {
                text = text.dropLast(2)
            }

            text
        }

        else -> value.toString()
    }
}