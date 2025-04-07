package org.hamersztein.klox.ast.statement.impl

import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.statement.Statement
import org.hamersztein.klox.ast.statement.Visitor

class Expression(val expression: Expression) : Statement() {

    override fun <R> accept(visitor: Visitor<R>) = visitor.visitExpressionStatement(this)

}