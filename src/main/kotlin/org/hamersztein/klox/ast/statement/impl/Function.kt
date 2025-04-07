package org.hamersztein.klox.ast.statement.impl

import org.hamersztein.klox.ast.statement.Statement
import org.hamersztein.klox.ast.statement.Visitor
import org.hamersztein.klox.token.Token

class Function(val name: Token, val params: List<Token>, val body: List<Statement>) : Statement() {

    override fun <R> accept(visitor: Visitor<R>) = visitor.visitFunctionStatement(this)

}