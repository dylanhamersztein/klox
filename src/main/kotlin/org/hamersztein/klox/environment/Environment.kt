package org.hamersztein.klox.environment

import org.hamersztein.klox.interpreter.RuntimeError
import org.hamersztein.klox.token.Token

class Environment(val enclosing: Environment? = null) {

    private val values = mutableMapOf<String, Any?>()

    operator fun set(name: String, value: Any?) {
        values[name] = value
    }

    operator fun set(name: Token, value: Any?) {
        when {
            values.containsKey(name.lexeme) -> {
                values[name.lexeme] = value
            }

            enclosing !== null -> {
                enclosing[name] = value
            }

            else -> throw runtimeError(name)
        }
    }

    operator fun get(name: Token): Any? =
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme]
        } else {
            enclosing?.get(name) ?: throw runtimeError(name)
        }

    private fun runtimeError(name: Token) = RuntimeError(name, "Undefined variable ${name.lexeme}")

}