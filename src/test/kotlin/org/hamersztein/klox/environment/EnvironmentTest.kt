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
    fun `should throw RuntimeError when attempting to fetch with a key that doesn't exist`() {
        val name = Token(IDENTIFIER, "breakfast", null, 1)

        val error = assertThrows<RuntimeError> {
            environment[name]
        }

        assertEquals(name, error.token)
        assertEquals("Undefined variable ${name.lexeme}", error.message)
    }

}