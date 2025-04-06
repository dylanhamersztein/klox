package org.hamersztein.klox.ast.expression.impl

import org.hamersztein.klox.ast.Visitor
import org.hamersztein.klox.ast.expression.Expression

class Ternary(val condition: Expression, val left: Expression, val right: Expression) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) =
        visitor.visitTernaryExpression(this)
}