package org.hamersztein.klox.ast.expression.impl

import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.expression.Visitor
import org.hamersztein.klox.token.Token

class Unary(val operator: Token, val right: Expression) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visitUnaryExpression(this)
}