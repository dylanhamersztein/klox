package org.hamersztein.klox.environment

import org.hamersztein.klox.interpreter.RuntimeError
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType.IDENTIFIER
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EnvironmentTest {

    private lateinit var environment: Environment

    @BeforeEach
    fun beforeEach() {
        environment = Environment()
    }

    @Test
    fun `should return a value when key exists`() {
        val name = "breakfast"
        val value = "muffin"

        environment[name] = value

        val fetchedValue = environment[Token(IDENTIFIER, name, null, 1)]

        assertEquals(value, fetchedValue)
    }

    @Test
    fun `should return a null value when key exists`() {
        val name = "breakfast"
        val value = null

        environment[name] = value

        val fetchedValue = environment[Token(IDENTIFIER, name, null, 1)]

        assertEquals(value, fetchedValue)
    }

    @Test
    fun `should assign a new value to existing environment value`() {
        val name = "breakfast"
        val value = "muffin"

        environment[name] = value

        val token = Token(IDENTIFIER, name, null, 1)
        environment[token] = "toast"

        assertEquals("toast", environment[token])
    }

    @Test
    fun `should get variable value from outer environment when inner does not contain key`() {
        val name = Token(IDENTIFIER, "breakfast", null, 1)

        val enclosingEnvironment = Environment()
        enclosingEnvironment[name.lexeme] = "muffin"

        environment = Environment(enclosingEnvironment)

        val fetchedValue = environment[name]

        assertEquals("muffin", fetchedValue)
    }

    @Test
    fun `should assign a new value to existing enclosing environment value`() {
        val name = "breakfast"
        val value = "muffin"

        val enclosingEnvironment = Environment()
        enclosingEnvironment[name] = value

        environment = Environment(enclosingEnvironment)

        val token = Token(IDENTIFIER, name, null, 1)
        environment[token] = "toast"

        assertEquals("toast", environment[token])
    }

    @Test
    fun `should throw RuntimeError when attempting to fetch with a key that doesn't exist`() {
        val name = Token(IDENTIFIER, "breakfast", null, 1)

        val error = assertThrows<RuntimeError> {
            environment[name]
        }

        assertEquals(name, error.token)
        assertEquals("Undefined variable ${name.lexeme}", error.message)
    }

    @Test
    fun `should throw RuntimeError when attempting to fetch with a key that doesn't exist in any environment`() {
        val name = Token(IDENTIFIER, "breakfast", null, 1)

        val enclosingEnvironment = Environment()
        environment = Environment(enclosingEnvironment)

        val error = assertThrows<RuntimeError> {
            environment[name]
        }

        assertEquals(name, error.token)
        assertEquals("Undefined variable ${name.lexeme}", error.message)
    }

    @Test
    fun `should throw RuntimeError when assigning a new value variable that does not exist in any environment`() {
        val enclosingEnvironment = Environment()
        environment = Environment(enclosingEnvironment)

        val token = Token(IDENTIFIER, "breakfast", null, 1)

        val error = assertThrows<RuntimeError> {
            environment[token] = "muffin"
        }

        assertEquals(token, error.token)
        assertEquals("Undefined variable ${token.lexeme}", error.message)
    }

}