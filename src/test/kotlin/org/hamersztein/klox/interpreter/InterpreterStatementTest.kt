package org.hamersztein.klox.interpreter

import org.hamersztein.klox.ast.expression.impl.*
import org.hamersztein.klox.ast.statement.impl.*
import org.hamersztein.klox.environment.Environment
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType.*
import org.hamersztein.klox.util.TestUtils.mockSystemOutStream
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.contracts.ExperimentalContracts
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExperimentalContracts
class InterpreterStatementTest {

    @Test
    fun `should interpret a variable statement with initializer and save it to the environment`() {
        val testEnvironment = Environment()

        val nameToken = Token(IDENTIFIER, "breakfast", null, 1)
        val initializer = Literal(3.0)
        val statements = listOf(Var(nameToken, initializer))

        val interpreter = Interpreter(testEnvironment)
        interpreter.interpret(statements)

        assertEquals(initializer.value, testEnvironment[nameToken])
    }

    @Test
    fun `should interpret a variable statement without initializer and save it to the environment`() {
        val testEnvironment = Environment()

        val nameToken = Token(IDENTIFIER, "breakfast", null, 1)
        val statements = listOf(Var(nameToken))

        val interpreter = Interpreter(testEnvironment)
        interpreter.interpret(statements)

        assertNull(testEnvironment[nameToken])
    }

    @Test
    fun `should interpret a print statement correctly`() {
        val (outputStreamCaptor, resetOutStream) = mockSystemOutStream()

        val literalValue = "hello, world!"
        val statements = listOf(Print(Literal(literalValue)))

        val interpreter = Interpreter()
        interpreter.interpret(statements)

        assertEquals(literalValue, outputStreamCaptor.toString().trim())

        resetOutStream()
    }

    @Test
    fun `should interpret a block statement`() {
        val (outputStreamCaptor, resetOutStream) = mockSystemOutStream()

        val blockStatement = Block(
            statements = listOf(
                Print(
                    Literal("hello, world!")
                )
            )
        )

        val interpreter = Interpreter()
        interpreter.interpret(listOf(blockStatement))

        assertEquals("hello, world!", outputStreamCaptor.toString().trim())

        resetOutStream()
    }

    @Test
    fun `should print the correct value when block statement reassigns global variable`() {
        val (outputStreamCaptor, resetOutStream) = mockSystemOutStream()

        val environment = Environment()
        environment.define("breakfast", "muffin")

        val blockStatement = Block(
            statements = listOf(
                Expression(
                    Assign(
                        Token(IDENTIFIER, "breakfast", null, 1),
                        Literal("toast")
                    )
                ),
                Print(
                    Variable(Token(IDENTIFIER, "breakfast", null, 1))
                )
            )
        )

        val interpreter = Interpreter(environment)
        interpreter.interpret(listOf(blockStatement))

        assertEquals("toast", outputStreamCaptor.toString().trim())

        resetOutStream()
    }

    @Test
    fun `should overwrite global scope variable value when assigning in block scope`() {
        val (outputStreamCaptor, resetOutStream) = mockSystemOutStream()

        val variableName = "breakfast"
        val globalValue = "muffin"
        val blockScopeValue = "toast"

        val statements = listOf(
            Var(
                Token(IDENTIFIER, variableName, null, 1),
                Literal(globalValue)
            ),
            Print(
                Variable(Token(IDENTIFIER, variableName, null, 3))
            )
        )

        val interpreter = Interpreter()
        interpreter.interpret(statements)

        assertEquals(globalValue, outputStreamCaptor.toString().trim())
        outputStreamCaptor.reset()

        val blockScopedStatements = listOf(
            Block(
                statements = listOf(
                    Expression(
                        Assign(
                            Token(IDENTIFIER, variableName, null, 3),
                            Literal(blockScopeValue)
                        )
                    ),
                    Print(
                        Variable(Token(IDENTIFIER, variableName, null, 3))
                    )
                )
            )
        )

        interpreter.interpret(blockScopedStatements)

        assertEquals(blockScopeValue, outputStreamCaptor.toString().trim())

        resetOutStream()
    }

    @Test
    fun `should shadow global scope variable value when declaring in block scope`() {
        val (outputStreamCaptor, resetOutStream) = mockSystemOutStream()

        val variableName = "breakfast"
        val globalValue = "muffin"
        val blockScopeValue = "toast"

        val globalScopeStatements = listOf(
            Var(
                Token(IDENTIFIER, variableName, null, 1),
                Literal(globalValue)
            ),
            Print(
                Variable(Token(IDENTIFIER, variableName, null, 3))
            )
        )

        val interpreter = Interpreter()
        interpreter.interpret(globalScopeStatements)

        assertEquals(globalValue, outputStreamCaptor.toString().trim())
        outputStreamCaptor.reset()

        val blockScopedStatements = listOf(
            Block(
                statements = listOf(
                    Var(
                        Token(IDENTIFIER, variableName, null, 1),
                        Literal(blockScopeValue)
                    ),
                    Print(
                        Variable(Token(IDENTIFIER, variableName, null, 3))
                    )
                )
            )
        )

        interpreter.interpret(blockScopedStatements)

        assertEquals(blockScopeValue, outputStreamCaptor.toString().trim())
        outputStreamCaptor.reset()

        val nextGlobalScopeStatements = listOf(
            Print(
                Variable(Token(IDENTIFIER, variableName, null, 3))
            )
        )

        interpreter.interpret(nextGlobalScopeStatements)
        assertEquals(globalValue, outputStreamCaptor.toString().trim())

        resetOutStream()
    }

    @Test
    fun `should allow redeclaration of variable in separate block scopes`() {
        val (outputStreamCaptor, resetOutStream) = mockSystemOutStream()

        val variableName = "breakfast"
        val firstBlockScopeValue = "toast"
        val secondBlockScopeValue = "muffin"

        val blockScopedStatements = listOf(
            Block(
                statements = listOf(
                    Var(
                        Token(IDENTIFIER, variableName, null, 1),
                        Literal(firstBlockScopeValue)
                    ),
                    Print(
                        Variable(Token(IDENTIFIER, variableName, null, 1))
                    )
                )
            ),
            Block(
                statements = listOf(
                    Var(
                        Token(IDENTIFIER, variableName, null, 1),
                        Literal(secondBlockScopeValue)
                    ),
                    Print(
                        Variable(Token(IDENTIFIER, variableName, null, 1))
                    )
                )
            )
        )

        val interpreter = Interpreter()
        interpreter.interpret(blockScopedStatements)

        assertEquals("$firstBlockScopeValue\n$secondBlockScopeValue", outputStreamCaptor.toString().trim())

        resetOutStream()
    }

    @Test
    fun `should have access to global variables inside block scope`() {
        val (outputStreamCaptor, resetOutStream) = mockSystemOutStream()

        val globalVariableName = "breakfast"
        val globalValue = "toast"

        val blockScopeVariableName = "drink"
        val blockScopeValue = "coffee"

        val statements = listOf(
            Var(
                Token(IDENTIFIER, globalVariableName, null, 1),
                Literal(globalValue)
            ),
            Block(
                statements = listOf(
                    Var(
                        Token(IDENTIFIER, blockScopeVariableName, null, 1),
                        Literal(blockScopeValue)
                    ),
                    Print(
                        Binary(
                            Variable(Token(IDENTIFIER, globalVariableName, null, 1)),
                            Token(PLUS, "+", null, 1),
                            Variable(Token(IDENTIFIER, blockScopeVariableName, null, 1))
                        )
                    )
                )
            )
        )

        val interpreter = Interpreter()
        interpreter.interpret(statements)

        assertEquals("$globalValue$blockScopeValue", outputStreamCaptor.toString().trim())

        resetOutStream()
    }

    @ValueSource(booleans = [true, false])
    @ParameterizedTest(name = "should execute correct branch of if statement when condition is literal '{0}'")
    fun `should execute correct branch of if statement based on literal condition`(conditionValue: Boolean) {
        val (outputStreamCaptor, resetOutStream) = mockSystemOutStream()

        val statements = listOf(
            If(
                Literal(conditionValue),
                Print(Literal("yay")),
                Print(Literal("boo")),
            )
        )

        val interpreter = Interpreter()
        interpreter.interpret(statements)

        assertEquals(if (conditionValue) "yay" else "boo", outputStreamCaptor.toString().trim())

        resetOutStream()
    }

    @Test
    fun `should execute correct branch of if statement based on expression condition`() {
        val (outputStreamCaptor, resetOutStream) = mockSystemOutStream()

        val statements = listOf(
            If(
                Binary(Literal(true), Token(EQUAL_EQUAL, "==", null, 1), Literal(false)),
                Print(Literal("yay")),
                Print(Literal("boo")),
            )
        )

        val interpreter = Interpreter()
        interpreter.interpret(statements)

        assertEquals("boo", outputStreamCaptor.toString().trim())

        resetOutStream()
    }

    @Test
    fun `should interpret while statement`() {
        val (outputStreamCaptor, resetOutStream) = mockSystemOutStream()

        val conditionToken = Token(IDENTIFIER, "condition", null, 1)
        val variableToken = Token(IDENTIFIER, "number", null, 1)

        val statements = listOf(
            Var(conditionToken, Literal(true)),
            Var(variableToken, Literal(1)),
            While(
                Logical(
                    Variable(conditionToken),
                    Token(EQUAL_EQUAL, "==", null, 1),
                    Literal(true)
                ),
                Block(
                    listOf(
                        Print(Variable(variableToken)),
                        Expression(Assign(conditionToken, Literal(false)))
                    )
                )
            )
        )

        val interpreter = Interpreter()
        interpreter.interpret(statements)

        assertEquals("1", outputStreamCaptor.toString().trim())

        resetOutStream()
    }
}
