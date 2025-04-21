package org.hamersztein.klox.ast.statement.impl

import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.statement.Statement
import org.hamersztein.klox.ast.statement.Visitor

class If(val condition: Expression, val thenBranch: Statement, val elseBranch: Statement? = null) : Statement() {

    override fun <R> accept(visitor: Visitor<R>) = visitor.visitIfStatement(this)

}