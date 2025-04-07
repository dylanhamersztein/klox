package org.hamersztein.klox.ast.statement

import org.hamersztein.klox.ast.statement.impl.*
import org.hamersztein.klox.ast.statement.impl.Function

interface Visitor<R> {
    fun visitBlockStatement(statement: Block): R
    fun visitClassStatement(statement: Class): R
    fun visitExpressionStatement(statement: Expression): R
    fun visitFunctionStatement(statement: Function): R
    fun visitIfStatement(statement: If): R
    fun visitPrintStatement(statement: Print): R
    fun visitReturnStatement(statement: Return): R
    fun visitVarStatement(statement: Var): R
    fun visitWhileStatement(statement: While): R
}