package org.hamersztein.klox.scanner

import org.hamersztein.klox.Lox
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType
import org.hamersztein.klox.token.TokenType.*

class Scanner(private val source: String) {

    private val tokens = mutableListOf<Token>()

    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(type = EOF, lexeme = "", literal = null, line = line))

        return tokens
    }

    private fun scanToken() {
        when (val c = advance()) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '"' -> string()
            
            '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) {
                        advance()
                    }
                } else {
                    addToken(SLASH)
                }
            }

            '\n' -> {
                line++
            }

            ' ' -> {}
            '\t' -> {}
            '\r' -> {}

            else -> {
                if (isDigit(c)) {
                    number()
                } else if (isAlpha(c)) {
                    identifier()
                } else {
                    Lox.error(line, "Unexpected character.")
                }
            }
        }
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) {
            advance()
        }

        val text = source.substring(start, current)
        val tokenType = keyWords[text] ?: IDENTIFIER

        addToken(tokenType)
    }

    private fun isAlphaNumeric(c: Char) = isAlpha(c) || isDigit(c)

    private fun isAlpha(c: Char) = c in 'a'..'z' || c in 'A'..'Z' || c == '_'

    private fun isDigit(char: Char) = char in '0'..'9'

    private fun number() {
        while (isDigit(peek())) {
            advance()
        }

        if (peek() == '.' && isDigit(peekNext())) {
            advance()

            while (isDigit(peek())) {
                advance()
            }
        }

        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    private fun peekNext() = if (current + 1 >= source.length) {
        '\u0000'
    } else {
        source[current + 1]
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++
            }

            advance()
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.")
        }

        // closing "
        advance()

        val value = source.substring(start + 1, current - 1)
        addToken(STRING, value)
    }

    private fun peek() = if (isAtEnd()) {
        '\u0000'
    } else {
        source[current]
    }

    private fun match(expected: Char): Boolean {
        return when {
            isAtEnd() || source[current] != expected -> false
            else -> {
                current++
                true
            }
        }
    }

    private fun advance() = source[current++]

    private fun addToken(tokenType: TokenType) {
        addToken(tokenType, null)
    }

    private fun addToken(tokenType: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(tokenType, text, literal, line))
    }

    private fun isAtEnd() = current >= source.length

    companion object {
        private val keyWords = mapOf(
            "and" to AND,
            "class" to CLASS,
            "if" to IF,
            "else" to ELSE,
            "false" to FALSE,
            "for" to FOR,
            "fun" to FUN,
            "nil" to NIL,
            "or" to OR,
            "print" to PRINT,
            "return" to RETURN,
            "super" to SUPER,
            "this" to THIS,
            "true" to TRUE,
            "var" to VAR,
            "while" to WHILE
        )
    }
}