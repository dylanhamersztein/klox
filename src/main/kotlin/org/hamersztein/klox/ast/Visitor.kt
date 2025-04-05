package org.hamersztein.klox.ast

import org.hamersztein.klox.ast.expression.impl.*
import org.hamersztein.klox.ast.expression.impl.Set

interface Visitor<R> {
    fun visitAssignExpr(expr: Assign): R
    fun visitBinaryExpr(expr: Binary): R
    fun visitCallExpr(expr: Call): R
    fun visitGetExpr(expr: Get): R
    fun visitGroupingExpr(expr: Grouping): R
    fun visitLiteralExpr(expr: Literal): R
    fun visitLogicalExpr(expr: Logical): R
    fun visitSetExpr(expr: Set): R
    fun visitSuperExpr(expr: Super): R
    fun visitThisExpr(expr: This): R
    fun visitUnaryExpr(expr: Unary): R
    fun visitVariableExpr(expr: Variable): R
}