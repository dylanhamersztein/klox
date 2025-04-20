package org.hamersztein.klox.interpreter

import org.hamersztein.klox.ast.expression.impl.*
import org.hamersztein.klox.ast.statement.impl.*
import org.hamersztein.klox.environment.Environment
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType
import org.hamersztein.klox.token.TokenType.*
import org.hamersztein.klox.util.TestUtils.mockSystemErrorStream
import org.hamersztein.klox.util.TestUtils.mockSystemOutStream
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.contracts.ExperimentalContracts
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExperimentalContracts
class InterpreterTest {

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
    fun `should log runtime error when attempting to reference variable that hasn't been declared`() {
        val (outputStreamCaptor, resetSystemError) = mockSystemErrorStream()

        val statements = listOf(Print(Variable(Token(IDENTIFIER, "breakfast", null, 1))))

        val interpreter = Interpreter()

        interpreter.interpret(statements)

        assertEquals("Undefined variable breakfast\n[line 1]", outputStreamCaptor.toString().trim())

        resetSystemError()
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
    fun `should interpret grouping expression correctly`() {
        val expression = Grouping(
            Binary(
                Literal(2.0),
                Token(STAR, "", null, 1),
                Literal(2.0)
            )
        )

        val interpreter = Interpreter()
        val result = interpreter.visitGroupingExpression(expression)

        assertEquals(4.0, result)
    }

    @Test
    fun `should interpret assign expression correctly when variable already exists in environment`() {
        val environment = Environment()
        environment.define("breakfast", "toast")

        val identifierToken = Token(IDENTIFIER, "breakfast", null, 1)

        val statements = listOf(
            Expression(
                Assign(
                    identifierToken,
                    Literal("muffin")
                )
            )
        )

        val interpreter = Interpreter(environment)
        interpreter.interpret(statements)

        assertEquals("muffin", environment[identifierToken])
    }

    @Test
    fun `should log error when assign expression refers to variable that does not exist`() {
        val (outputStream, resetSystemError) = mockSystemErrorStream()

        val identifierToken = Token(IDENTIFIER, "breakfast", null, 1)

        val statements = listOf(
            Expression(
                Assign(
                    identifierToken,
                    Literal("muffin")
                )
            )
        )

        val interpreter = Interpreter()

        interpreter.interpret(statements)

        assertEquals("Undefined variable breakfast\n[line 1]", outputStream.toString().trim())

        resetSystemError()
    }

    @MethodSource("provideArgumentsForLiteralTest")
    @ParameterizedTest(name = "should interpret literal expression whose value is {0}")
    fun `should interpret literal expression`(value: Any?) {
        val expression = Literal(value)

        val interpreter = Interpreter()
        val result = interpreter.visitLiteralExpression(expression)

        assertEquals(expression.value, result)
    }

    @MethodSource("provideArgumentsForUnaryTest")
    @ParameterizedTest(name = "should interpret unary expression and return {2} when operator token is {0} and literal is {1}")
    fun `should interpret unary expression`(tokenType: TokenType, literal: Any?, expected: Any) {
        val expression = Unary(Token(tokenType, "", null, 1), Literal(literal))

        val interpreter = Interpreter()
        val result = interpreter.visitUnaryExpression(expression)

        assertEquals(expected, result)
    }

    @MethodSource("provideArgumentsForBinaryTest")
    @ParameterizedTest(name = "should interpret binary expression and return {3} when operator is {0}, left is {1}, and right is {2}")
    fun `should interpret binary expression`(tokenType: TokenType, left: Any?, right: Any?, expected: Any) {
        val expression = Binary(
            Literal(left),
            Token(tokenType, "", null, 1),
            Literal(right)
        )

        val interpreter = Interpreter()
        val result = interpreter.visitBinaryExpression(expression)

        assertEquals(expected, result)
    }

    @Test
    fun `should throw RuntimeError when interpreting unary minus where operand is not a number`() {
        val token = Token(MINUS, "", null, 1)
        val expression = Unary(token, Literal("muffin"))

        val interpreter = Interpreter()

        val exception = assertThrows<RuntimeError> {
            interpreter.visitUnaryExpression(expression)
        }

        assertEquals(token, exception.token)
        assertEquals("Operand must be a number.", exception.message)
    }

    @ParameterizedTest(name = "should throw runtime error when interpreting binary expression with operator {0}, left {1}, and right {2}")
    @MethodSource("provideArgumentsForBinaryWithBadOperandsTest")
    fun `should throw RuntimeError when interpreting binary expressions with bad operands`(
        tokenType: TokenType,
        left: Any?,
        right: Any?,
        expectedErrorMessage: String
    ) {
        val token = Token(tokenType, "", null, 1)
        val expression = Binary(Literal(left), token, Literal(right))

        val interpreter = Interpreter()

        val exception = assertThrows<RuntimeError> {
            interpreter.visitBinaryExpression(expression)
        }

        assertEquals(token, exception.token)
        assertEquals(expectedErrorMessage, exception.message)

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
    fun `should throw error when declaring variable in block scope and referencing it outside block scope`() {
        val (outputStreamCaptor, resetOutStream) = mockSystemOutStream()
        val (errorStreamCaptor, resetErrorStream) = mockSystemErrorStream()

        val variableName = "breakfast"
        val blockScopeValue = "toast"

        val blockScopedStatements = listOf(
            Block(
                statements = listOf(
                    Var(
                        Token(IDENTIFIER, variableName, null, 1),
                        Literal(blockScopeValue)
                    ),
                    Print(
                        Variable(Token(IDENTIFIER, variableName, null, 1))
                    )
                )
            )
        )

        val interpreter = Interpreter()
        interpreter.interpret(blockScopedStatements)

        assertEquals(blockScopeValue, outputStreamCaptor.toString().trim())
        outputStreamCaptor.reset()

        val globalStatements = listOf(
            Print(
                Variable(Token(IDENTIFIER, variableName, null, 2))
            )
        )

        interpreter.interpret(globalStatements)

        assertTrue(outputStreamCaptor.toString().trim().isEmpty())
        assertEquals("Undefined variable $variableName\n[line 2]", errorStreamCaptor.toString().trim())

        resetOutStream()
        resetErrorStream()
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
    fun `should visit logical expression when operator is AND`() {
        val (outputStreamCaptor, resetOutStream) = mockSystemOutStream()

        val statements = listOf(
            If(
                Logical(
                    Literal(true),
                    Token(AND, "and", null, 1),
                    Literal(false)
                ),
                Print(Literal("true")),
                Print(Literal("false"))
            )
        )

        val interpreter = Interpreter()
        interpreter.interpret(statements)

        assertEquals("false", outputStreamCaptor.toString().trim())

        resetOutStream()
    }

    @Test
    fun `should visit logical expression when operator is OR`() {
        val (outputStreamCaptor, resetOutStream) = mockSystemOutStream()

        val statements = listOf(
            If(
                Logical(
                    Literal(true),
                    Token(OR, "and", null, 1),
                    Literal(false)
                ),
                Print(Literal("true")),
                Print(Literal("false"))
            )
        )

        val interpreter = Interpreter()
        interpreter.interpret(statements)

        assertEquals("true", outputStreamCaptor.toString().trim())

        resetOutStream()
    }

    @ValueSource(booleans = [true, false])
    @ParameterizedTest(name = "should execute correct branch of ternary expression when condition is literal '{0}'")
    fun `should execute correct branch of ternary expression based on literal condition`(conditionValue: Boolean) {
        val environment = Environment()

        val variableNameToken = Token(IDENTIFIER, "breakfast", null, 1)

        val statements = listOf(
            Var(
                variableNameToken,
                Ternary(
                    Literal(conditionValue),
                    Literal("toast"),
                    Literal("muffin")
                )
            )
        )

        val interpreter = Interpreter(environment)
        interpreter.interpret(statements)

        assertEquals(if (conditionValue) "toast" else "muffin", environment.get(variableNameToken))
    }

    @Test
    fun `should execute correct branch of ternary expression based on expression condition`() {
        val environment = Environment()

        val variableNameToken = Token(IDENTIFIER, "breakfast", null, 1)

        val statements = listOf(
            Var(
                variableNameToken,
                Ternary(
                    Binary(Literal(true), Token(EQUAL_EQUAL, "==", null, 1), Literal(false)),
                    Literal("toast"),
                    Literal("muffin")
                )
            )
        )

        val interpreter = Interpreter(environment)
        interpreter.interpret(statements)

        assertEquals("muffin", environment.get(variableNameToken))
    }

    companion object {
        @JvmStatic
        private fun provideArgumentsForLiteralTest() = listOf(
            Arguments.of(1.0),
            Arguments.of("hello"),
            Arguments.of(null),
        )

        @JvmStatic
        private fun provideArgumentsForUnaryTest() = listOf(
            Arguments.of(MINUS, 1.0, -1.0),
            Arguments.of(BANG, false, true),
        )

        @JvmStatic
        private fun provideArgumentsForBinaryTest() = listOf(
            Arguments.of(MINUS, 1.0, 2.0, -1.0),
            Arguments.of(SLASH, 2.0, 1.0, 2.0),
            Arguments.of(STAR, 2.0, 2.0, 4.0),
            Arguments.of(PLUS, 2.0, 2.0, 4.0),
            Arguments.of(PLUS, "hello", "world", "helloworld"),
            Arguments.of(PLUS, "hello", 2.1, "hello2.1"),
            Arguments.of(PLUS, 2.1, "hello", "2.1hello"),
            Arguments.of(PLUS, "hello", 2.0, "hello2"),
            Arguments.of(PLUS, 2.0, "hello", "2hello"),
            Arguments.of(PLUS, true, "hello", "truehello"),
            Arguments.of(PLUS, "hello", true, "hellotrue"),
            Arguments.of(GREATER, 1.0, 2.0, false),
            Arguments.of(GREATER_EQUAL, 1.0, 2.0, false),
            Arguments.of(LESS, 1.0, 2.0, true),
            Arguments.of(LESS_EQUAL, 1.0, 2.0, true),
            Arguments.of(BANG_EQUAL, null, null, false),
            Arguments.of(BANG_EQUAL, null, 2.0, true),
            Arguments.of(BANG_EQUAL, 2.0, 2.0, false),
            Arguments.of(BANG_EQUAL, 2.0, 1.0, true),
            Arguments.of(BANG_EQUAL, "hello", "hello", false),
            Arguments.of(BANG_EQUAL, "hello", "world", true),
            Arguments.of(EQUAL_EQUAL, null, null, true),
            Arguments.of(EQUAL_EQUAL, null, 2.0, false),
            Arguments.of(EQUAL_EQUAL, 2.0, 2.0, true),
            Arguments.of(EQUAL_EQUAL, 2.0, 1.0, false),
            Arguments.of(EQUAL_EQUAL, "hello", "hello", true),
            Arguments.of(EQUAL_EQUAL, "hello", "world", false),
        )

        @JvmStatic
        private fun provideArgumentsForBinaryWithBadOperandsTest() = listOf(
            Arguments.of(MINUS, "hello", 1.0, "Operand must be a number."),
            Arguments.of(MINUS, 1.0, "hello", "Operand must be a number."),
            Arguments.of(STAR, "hello", 1.0, "Operand must be a number."),
            Arguments.of(STAR, 1.0, "hello", "Operand must be a number."),
            Arguments.of(SLASH, "hello", 1.0, "Operand must be a number."),
            Arguments.of(SLASH, 1.0, "hello", "Operand must be a number."),
            Arguments.of(SLASH, 1.0, 0.0, "Cannot divide by 0"),
            Arguments.of(PLUS, false, true, "Could not add false to true."),
            Arguments.of(PLUS, 1.0, true, "Could not add 1 to true."),
            Arguments.of(PLUS, 1.1, true, "Could not add 1.1 to true.")
        )
    }

}