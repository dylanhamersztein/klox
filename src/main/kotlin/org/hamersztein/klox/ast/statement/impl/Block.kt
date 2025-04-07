package org.hamersztein.klox.ast.statement.impl

import org.hamersztein.klox.ast.statement.Statement
import org.hamersztein.klox.ast.statement.Visitor

class Block(val statements: List<Statement>) : Statement() {

    override fun <R> accept(visitor: Visitor<R>) = visitor.visitBlockStatement(this)

}
