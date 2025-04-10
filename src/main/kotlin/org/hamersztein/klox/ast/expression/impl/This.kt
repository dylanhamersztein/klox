package org.hamersztein.klox.ast.expression.impl

import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.expression.Visitor
import org.hamersztein.klox.token.Token

class This(val keyword: Token) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visitThisExpression(this)
}