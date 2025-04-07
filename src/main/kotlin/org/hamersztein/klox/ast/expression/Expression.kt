package org.hamersztein.klox.ast.expression

abstract class Expression {
    abstract fun <R> accept(visitor: Visitor<R>): R
}