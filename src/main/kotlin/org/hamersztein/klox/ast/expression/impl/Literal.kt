package org.hamersztein.klox.ast.expression.impl

import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.expression.Visitor

class Literal(val value: Any?) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visitLiteralExpression(this)

    override fun equals(other: Any?) = when {
        this === other -> true
        other !is Literal -> false
        else -> value == other.value
    }

    override fun hashCode() = value?.hashCode() ?: 0
}