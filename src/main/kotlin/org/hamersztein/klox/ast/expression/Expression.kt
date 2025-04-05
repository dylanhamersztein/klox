package org.hamersztein.klox.ast.expression

import org.hamersztein.klox.ast.Visitor

abstract class Expression {
    abstract fun <R> accept(visitor: Visitor<R>): R
}