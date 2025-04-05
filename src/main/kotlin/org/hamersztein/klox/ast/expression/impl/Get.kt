package org.hamersztein.klox.ast.expression.impl

import org.hamersztein.klox.ast.Visitor
import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.token.Token

class Get(val obj: Expression, val name: Token) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visitGetExpr(this)
}