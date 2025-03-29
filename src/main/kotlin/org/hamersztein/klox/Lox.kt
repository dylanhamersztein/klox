package org.hamersztein.klox

import org.hamersztein.klox.scanner.Scanner
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

        private fun run(program: String) {
            val scanner = Scanner(program)
            val tokens = scanner.scanTokens()

            tokens.forEach { println(it) }
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