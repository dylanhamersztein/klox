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
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import org.hamersztein.klox.ast.expression.Visitor as ExpressionVisitor
import org.hamersztein.klox.ast.statement.Visitor as StatementVisitor
import org.hamersztein.klox.ast.statement.impl.Expression as ExpressionStatement

@ExperimentalContracts
class Interpreter(private var environment: Environment = Environment()) : ExpressionVisitor<Any?>,
    StatementVisitor<Unit> {

    fun interpret(statements: List<Statement?>) {
        try {
            statements.forEach(::execute)
        } catch (e: RuntimeError) {
            Lox.runtimeError(e)
        }
    }

    fun interpret(statement: Statement?) {
        try {
            execute(statement)
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
                -right
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
                left - right
            }

            SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                checkRightIsNotZero(expr.operator, right)
                left / right
            }

            STAR -> {
                checkNumberOperands(expr.operator, left, right)
                left * right
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
                left > right
            }

            GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                left >= right
            }

            LESS -> {
                checkNumberOperands(expr.operator, left, right)
                left < right
            }

            LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                left <= right
            }

            BANG_EQUAL -> !isEqual(left, right)
            EQUAL_EQUAL -> isEqual(left, right)

            else -> null
        }
    }

    override fun visitVariableExpression(expr: Variable) = environment[expr.name]

    override fun visitAssignExpression(expr: Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)

        return value
    }

    override fun visitTernaryExpression(expr: Ternary): Any? {
        val condition = evaluate(expr.condition)

        return evaluate(
            if (isTruthy(condition)) {
                expr.left
            } else {
                expr.right
            }
        )
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

    override fun visitExpressionStatement(statement: ExpressionStatement) {
        evaluate(statement.expression)
    }

    override fun visitPrintStatement(statement: Print) {
        val value = evaluate(statement.expression)
        println(stringify(value))
    }

    override fun visitIfStatement(statement: If) {
        val condition = evaluate(statement.condition)

        execute(
            if (isTruthy(condition)) {
                statement.thenBranch
            } else {
                statement.elseBranch
            }
        )
    }

    override fun visitVarStatement(statement: Var) {
        environment.define(statement.name.lexeme, statement.initializer?.let(::evaluate))
    }

    override fun visitBlockStatement(statement: Block) {
        executeBlock(statement.statements, Environment(environment))
    }

    override fun visitClassStatement(statement: Class) {
        TODO("Not yet implemented")
    }

    override fun visitFunctionStatement(statement: Function) {
        TODO("Not yet implemented")
    }

    override fun visitReturnStatement(statement: Return) {
        TODO("Not yet implemented")
    }

    override fun visitWhileStatement(statement: While) {
        TODO("Not yet implemented")
    }

    private fun executeBlock(statements: List<Statement?>, environment: Environment) {
        val previousEnvironment = this.environment

        try {
            this.environment = environment
            statements.forEach(::execute)
        } finally {
            this.environment = previousEnvironment
        }
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

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        contract {
            returns() implies (left is Double && right is Double)
        }

        if (left !is Double || right !is Double) {
            throw RuntimeError(operator, "Operand must be a number.")
        }
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        contract {
            returns() implies (operand is Double)
        }

        if (operand !is Double) {
            throw RuntimeError(operator, "Operand must be a number.")
        }
    }

    private fun checkRightIsNotZero(token: Token, right: Double) {
        if (right == 0.0) {
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