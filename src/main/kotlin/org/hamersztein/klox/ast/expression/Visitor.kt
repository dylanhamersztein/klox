package org.hamersztein.klox.ast.expression

import org.hamersztein.klox.ast.expression.impl.*
import org.hamersztein.klox.ast.expression.impl.Set

interface Visitor<R> {
    fun visitAssignExpression(expr: Assign): R
    fun visitBinaryExpression(expr: Binary): R
    fun visitCallExpression(expr: Call): R
    fun visitGetExpression(expr: Get): R
    fun visitGroupingExpression(expr: Grouping): R
    fun visitLiteralExpression(expr: Literal): R
    fun visitLogicalExpression(expr: Logical): R
    fun visitSetExpression(expr: Set): R
    fun visitSuperExpression(expr: Super): R
    fun visitThisExpression(expr: This): R
    fun visitUnaryExpression(expr: Unary): R
    fun visitVariableExpression(expr: Variable): R
    fun visitTernaryExpression(expr: Ternary): R
}