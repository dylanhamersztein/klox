package org.hamersztein.klox

import org.hamersztein.klox.ast.AstPrinter
import org.hamersztein.klox.parser.Parser
import org.hamersztein.klox.scanner.Scanner
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class Lox {
    companion object {
        private var hadError = false

        fun runFile(filePath: String) {
            val bytes = Files.readAllBytes(Paths.get(filePath))
            run(bytes.toString(Charset.defaultCharset()))

            if (hadError) {
                exitProcess(65)
            }
        }

        fun runPrompt() {
            val input = InputStreamReader(System.`in`)
            val reader = BufferedReader(input)

            while (true) {
                print("> ")

                val line = reader.readLine() ?: break
                run(line)

                hadError = false
            }
        }

        fun error(line: Int, message: String) {
            report(line, "", message)
        }

        fun error(token: Token, message: String) {
            if (token.type == TokenType.EOF) {
                report(token.line, "at end", message)
            } else {
                report(token.line, " at '${token.lexeme}'", message)
            }
        }

        private fun run(program: String) {
            val scanner = Scanner(program)
            val parser = Parser(scanner.scanTokens())

            val expression = parser.parse()

            if (hadError) {
                return
            }

            println(AstPrinter().print(expression!!))
        }

        private fun report(line: Int, where: String, message: String) {
            System.err.println("[$line]: Error $where: $message")
            hadError = true
        }
    }
}

fun main(args: Array<String>) {
    when {
        args.size > 1 -> {
            println("Usage: jlox [script]")
            exitProcess(64)
        }

        args.size == 1 -> {
            Lox.runFile(args[0])
        }

        else -> {
            Lox.runPrompt()
        }
    }
}