package org.hamersztein.klox.ast.statement.impl

import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.statement.Statement
import org.hamersztein.klox.ast.statement.Visitor
import org.hamersztein.klox.token.Token

class Return(val keyword: Token, val value: Expression) : Statement() {

    override fun <R> accept(visitor: Visitor<R>) = visitor.visitReturnStatement(this)

}