package org.hamersztein.klox.token

class Token(val type: TokenType, val lexeme: String, val literal: Any?, val line: Int) {

    override fun toString() = "$type $lexeme $literal"

    override fun equals(other: Any?) = when {
        this === other -> true
        other !is Token -> false
        else -> line == other.line &&
                type == other.type &&
                lexeme == other.lexeme &&
                literal == other.literal
    }

    override fun hashCode() = arrayOf(line, type, lexeme, literal).contentHashCode()
}