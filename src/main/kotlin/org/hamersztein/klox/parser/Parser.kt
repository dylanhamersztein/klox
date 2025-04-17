package org.hamersztein.klox.parser

import org.hamersztein.klox.Lox
import org.hamersztein.klox.ast.expression.Expression
import org.hamersztein.klox.ast.expression.impl.*
import org.hamersztein.klox.ast.statement.Statement
import org.hamersztein.klox.ast.statement.impl.Block
import org.hamersztein.klox.ast.statement.impl.Print
import org.hamersztein.klox.ast.statement.impl.Var
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType
import org.hamersztein.klox.token.TokenType.*
import kotlin.contracts.ExperimentalContracts
import org.hamersztein.klox.ast.statement.impl.Expression as ExpressionStatement

@ExperimentalContracts
class Parser(private val tokens: List<Token>) {
    private var current = 0

    fun parse() = mutableListOf<Statement?>().apply {
        while (!isAtEnd()) {
            this += declaration()
        }
    }

    private fun declaration() = try {
        when {
            match(VAR) -> variableDeclaration()
            else -> statement()
        }
    } catch (_: ParseError) {
        synchronize()
        null
    }

    private fun variableDeclaration(): Statement {
        val name = consume(IDENTIFIER, "Expect variable name.")
        val initializer = if (match(EQUAL)) expression() else null

        consume(SEMICOLON, "Expect ';' after variable declaration.")

        return Var(name, initializer)
    }

    private fun statement() = when {
        match(PRINT) -> printStatement()
        match(LEFT_BRACE) -> Block(block())
        else -> expressionStatement()
    }

    private fun printStatement(): Statement {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value.")

        return Print(value)
    }

    private fun block(): List<Statement?> {
        val statements = mutableListOf<Statement?>()

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements += declaration()
        }

        consume(RIGHT_BRACE, "Expect '}' after block.")

        return statements.toList()
    }

    private fun expressionStatement(): Statement {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after expression.")

        return ExpressionStatement(value)
    }

    private fun expression() = assignment()

    private fun assignment(): Expression {
        val expression = ternary()

        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expression is Variable) {
                val name = expression.name
                return Assign(name, value)
            }

            error(equals, "Invalid assignment target.");
        }

        return expression
    }

    private fun ternary(): Expression {
        var expression = equality()

        if (match(QUESTION_MARK)) {
            val left = expression()
            consume(COLON, "Expect : after ternary expression.")
            val right = expression()

            expression = Ternary(expression, left, right)
        }

        return expression
    }

    private fun equality() = parseBinaryOperators(::comparison, BANG_EQUAL, EQUAL_EQUAL)

    private fun comparison() = parseBinaryOperators(::term, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)

    private fun term() = parseBinaryOperators(::factor, MINUS, PLUS)

    private fun factor() = parseBinaryOperators(::unary, SLASH, STAR)

    private fun unary(): Expression = if (match(BANG, MINUS)) {
        val operator = previous()
        val right = unary()
        Unary(operator, right)
    } else {
        primary()
    }

    private fun primary() = when {
        match(FALSE) -> Literal(false)
        match(TRUE) -> Literal(true)
        match(NIL) -> Literal(null)
        match(NUMBER, STRING) -> Literal(previous().literal)
        match(IDENTIFIER) -> Variable(previous())

        match(LEFT_PAREN) -> {
            val expression = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            Grouping(expression)
        }

        else -> throw error(peek(), "Expect expression.")
    }

    private fun parseBinaryOperators(expressionSupplier: () -> Expression, vararg tokenTypes: TokenType): Expression {
        var expression = expressionSupplier()

        while (match(*tokenTypes)) {
            val operator = previous()
            val right = expressionSupplier()
            expression = Binary(expression, operator, right)
        }

        return expression
    }

    private fun match(vararg tokenTypes: TokenType): Boolean {
        tokenTypes.forEach {
            if (check(it)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun consume(tokenType: TokenType, message: String): Token {
        if (check(tokenType)) {
            return advance()
        }

        throw error(peek(), message)
    }

    private fun check(tokenType: TokenType) = !isAtEnd() && peek().type == tokenType

    private fun advance(): Token {
        if (!isAtEnd()) {
            current++
        }

        return previous()
    }

    private fun isAtEnd() = peek().type == EOF

    private fun peek() = tokens[current]

    private fun previous() = tokens[current - 1]

    private fun error(token: Token, message: String) =
        ParseError().also {
            Lox.error(token, message)
        }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) {
                return
            }

            when (peek().type) {
                CLASS,
                FUN,
                VAR,
                FOR,
                IF,
                WHILE,
                PRINT,
                RETURN -> return

                else -> advance()
            }
        }
    }
}