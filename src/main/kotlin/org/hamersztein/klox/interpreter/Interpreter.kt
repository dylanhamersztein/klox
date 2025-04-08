package org.hamersztein.klox.interpreter

import org.hamersztein.klox.Lox
import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.expression.impl.*
import org.hamersztein.klox.ast.expression.impl.Set
import org.hamersztein.klox.ast.statement.Statement
import org.hamersztein.klox.ast.statement.impl.*
import org.hamersztein.klox.ast.statement.impl.Function
import org.hamersztein.klox.environment.Environment
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType.*
import org.hamersztein.klox.ast.expression.Visitor as ExpressionVisitor
import org.hamersztein.klox.ast.statement.Visitor as StatementVisitor
import org.hamersztein.klox.ast.statement.impl.Expression as ExpressionStatement

class Interpreter(private val env: Environment = Environment()) : ExpressionVisitor<Any?>, StatementVisitor<Unit> {

    fun interpret(statements: List<Statement?>) {
        try {
            statements.forEach(::execute)
        } catch (e: RuntimeError) {
            Lox.runtimeError(e)
        }
    }

    override fun visitLiteralExpression(expr: Literal) = expr.value

    override fun visitGroupingExpression(expr: Grouping) = evaluate(expr.expression)

    override fun visitUnaryExpression(expr: Unary): Any? {
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

    override fun visitBinaryExpression(expr: Binary): Any? {
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
                left is String || right is String -> {
                    val leftString = left.toString()

                    when {
                        left is Double && leftString.endsWith(DOUBLE_SUFFIX_IDENTIFIER) -> {
                            leftString.dropLast(2) + right
                        }

                        right is Double && right.toString().endsWith(DOUBLE_SUFFIX_IDENTIFIER) -> {
                            leftString + right.toString().dropLast(2)
                        }

                        else -> leftString + right
                    }
                }

                else -> handleBadPlusOperands(left, right, expr)
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

    override fun visitVariableExpression(expr: Variable) = env[expr.name]

    override fun visitAssignExpression(expr: Assign): Any? {
        TODO("Not yet implemented")
    }

    override fun visitCallExpression(expr: Call): Any? {
        TODO("Not yet implemented")
    }

    override fun visitGetExpression(expr: Get): Any? {
        TODO("Not yet implemented")
    }

    override fun visitLogicalExpression(expr: Logical): Any? {
        TODO("Not yet implemented")
    }

    override fun visitSetExpression(expr: Set): Any? {
        TODO("Not yet implemented")
    }

    override fun visitSuperExpression(expr: Super): Any? {
        TODO("Not yet implemented")
    }

    override fun visitThisExpression(expr: This): Any? {
        TODO("Not yet implemented")
    }

    override fun visitTernaryExpression(expr: Ternary): Any? {
        TODO("Not yet implemented")
    }

    override fun visitExpressionStatement(statement: ExpressionStatement) {
        evaluate(statement.expression)
    }

    override fun visitPrintStatement(statement: Print) {
        val value = evaluate(statement.expression)
        println(stringify(value))
    }

    override fun visitVarStatement(statement: Var) {
        env[statement.name.lexeme] = statement.initializer?.let(::evaluate)
    }

    override fun visitBlockStatement(statement: Block) {
        TODO("Not yet implemented")
    }

    override fun visitClassStatement(statement: Class) {
        TODO("Not yet implemented")
    }

    override fun visitFunctionStatement(statement: Function) {
        TODO("Not yet implemented")
    }

    override fun visitIfStatement(statement: If) {
        TODO("Not yet implemented")
    }

    override fun visitReturnStatement(statement: Return) {
        TODO("Not yet implemented")
    }

    override fun visitWhileStatement(statement: While) {
        TODO("Not yet implemented")
    }

    private fun execute(statement: Statement?) = statement?.accept(this)

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

            if (text.endsWith(DOUBLE_SUFFIX_IDENTIFIER)) {
                text = text.dropLast(2)
            }

            text
        }

        else -> value.toString()
    }

    private fun handleBadPlusOperands(left: Any?, right: Any?, expr: Binary) {
        val leftText = if (left is Double && left.toString().endsWith(DOUBLE_SUFFIX_IDENTIFIER)) {
            left.toString().dropLast(2)
        } else {
            left
        }

        val rightText = if (right is Double && right.toString().endsWith(DOUBLE_SUFFIX_IDENTIFIER)) {
            right.toString().dropLast(2)
        } else {
            right
        }

        throw RuntimeError(expr.operator, "Could not add $leftText to $rightText.")
    }

    companion object {
        private const val DOUBLE_SUFFIX_IDENTIFIER = ".0"
    }
}