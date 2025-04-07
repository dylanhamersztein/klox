package org.hamersztein.klox.interpreter

import org.hamersztein.klox.token.Token

class RuntimeError(val token: Token, message: String) : RuntimeException(message)