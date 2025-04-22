package org.hamersztein.klox

import org.hamersztein.klox.util.TestUtils.mockSystemOutStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class LoxTest {

    @Test
    fun `should print first 21 elements of the fibonacci sequence`() {
        val (outputStreamCaptor, resetOutputStream) = mockSystemOutStream()

        Lox.runFile("src/test/resources/fibonacci.lox")

        assertEquals(
            "0\n1\n1\n2\n3\n5\n8\n13\n21\n34\n55\n89\n144\n233\n377\n610\n987\n1597\n2584\n4181\n6765",
            outputStreamCaptor.toString().trim()
        )

        resetOutputStream()
    }

}
