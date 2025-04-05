package org.hamersztein.klox.ast.expression.impl

import org.hamersztein.klox.ast.Visitor
import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.token.Token

class Logical(val left: Expression, val operator: Token, val right: Expression) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visitLogicalExpr(this)
}