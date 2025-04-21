package org.hamersztein.klox.ast.expression.impl

import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.expression.Visitor
import org.hamersztein.klox.token.Token

class Variable(val name: Token) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visitVariableExpression(this)

    override fun equals(other: Any?) = when {
        this === other -> true
        other !is Variable -> false
        else -> name == other.name
    }

    override fun hashCode() = name.hashCode()
}