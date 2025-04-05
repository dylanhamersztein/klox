package org.hamersztein.klox.ast.expression.impl

import org.hamersztein.klox.ast.Visitor
import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.token.Token

class Assign(val name: Token, val value: Expression) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visitAssignExpr(this)
}