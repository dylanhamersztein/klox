package org.hamersztein.klox.ast.expression.impl

import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.expression.Visitor

class Literal(val value: Any?) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visitLiteralExpr(this)
}