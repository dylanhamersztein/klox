package org.hamersztein.klox.ast.statement.impl

import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.statement.Statement
import org.hamersztein.klox.ast.statement.Visitor

class While(val condition: Expression, val body: Statement) : Statement() {

    override fun <R> accept(visitor: Visitor<R>) = visitor.visitWhileStatement(this)

}