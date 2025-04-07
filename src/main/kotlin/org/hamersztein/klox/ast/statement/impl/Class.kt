package org.hamersztein.klox.ast.statement.impl

import org.hamersztein.klox.ast.expression.impl.Variable
import org.hamersztein.klox.ast.statement.Statement
import org.hamersztein.klox.ast.statement.Visitor
import org.hamersztein.klox.token.Token

class Class(val name: Token, val superClass: Variable, methods: List<Function>) : Statement() {

    override fun <R> accept(visitor: Visitor<R>) = visitor.visitClassStatement(this)

}