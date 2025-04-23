package org.hamersztein.klox.scanner

import org.hamersztein.klox.token.TokenType.EOF
import org.hamersztein.klox.util.TestUtils.mockSystemErrorStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class ScannerErrorTest {

    @Test
    fun `should log error and return EOF token when scanning unrecognised character`() {
        val (streamCaptor, resetSystemError) = mockSystemErrorStream()

        val program = "^"

        val scanner = Scanner(program)
        val tokens = scanner.scanTokens()

        assertEquals(1, tokens.size)
        assertEquals(EOF, tokens[0].type)

        assertEquals("[1]: Error : Unexpected character.", streamCaptor.toString().trim())

        resetSystemError()
    }

    @Test
    fun `should log error and return EOF token when scanning unterminated string`() {
        val (outputStreamCaptor, resetSystemError) = mockSystemErrorStream()

        val program = """"some string value"""

        val scanner = Scanner(program)
        val tokens = scanner.scanTokens()

        assertEquals(1, tokens.size)
        assertEquals(EOF, tokens[0].type)

        assertEquals("[1]: Error : Unterminated string.", outputStreamCaptor.toString().trim())

        resetSystemError()
    }

}
