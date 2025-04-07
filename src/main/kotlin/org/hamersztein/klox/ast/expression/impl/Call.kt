package org.hamersztein.klox.ast.expression.impl

import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.expression.Visitor
import org.hamersztein.klox.token.Token

class Call(val callee: Expression, val paren: Token, val arguments: List<Expression>) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visitCallExpr(this)
}