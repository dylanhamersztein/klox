package org.hamersztein.klox.ast.statement

abstract class Statement {

    abstract fun <R> accept(visitor: Visitor<R>): R

}