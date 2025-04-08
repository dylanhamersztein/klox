package org.hamersztein.klox

import org.hamersztein.klox.interpreter.Interpreter
import org.hamersztein.klox.interpreter.RuntimeError
import org.hamersztein.klox.parser.Parser
import org.hamersztein.klox.scanner.Scanner
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.contracts.ExperimentalContracts
import kotlin.system.exitProcess

@ExperimentalContracts
class Lox {
    companion object {
        private var hadError = false
        private var hadRuntimeError = false

        private val interpreter = Interpreter()

        fun runFile(filePath: String) {
            val bytes = Files.readAllBytes(Paths.get(filePath))
            run(bytes.toString(Charset.defaultCharset()))

            if (hadError) {
                exitProcess(65)
            }

            if (hadRuntimeError) {
                exitProcess(70)
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

        private fun run(program: String) {
            val scanner = Scanner(program)
            val parser = Parser(scanner.scanTokens())

            val statements = parser.parse()

            if (hadError) {
                return
            }

            interpreter.interpret(statements)
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

        fun runtimeError(e: RuntimeError) {
            System.err.println("${e.message}\n[line ${e.token.line}]")
            hadRuntimeError = true
        }

        private fun report(line: Int, where: String, message: String) {
            System.err.println("[$line]: Error $where: $message")
            hadError = true
        }
    }
}

@ExperimentalContracts
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