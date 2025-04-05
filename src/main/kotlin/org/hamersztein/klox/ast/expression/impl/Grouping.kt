package org.hamersztein.klox.ast.expression.impl

import org.hamersztein.klox.ast.Visitor
import org.hamersztein.klox.ast.expression.Expression

class Grouping(val expression: Expression) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visitGroupingExpr(this)
}