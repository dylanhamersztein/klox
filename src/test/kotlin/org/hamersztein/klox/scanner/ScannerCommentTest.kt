package org.hamersztein.klox.scanner

import org.hamersztein.klox.scanner.ScannerTestUtils.assertProgram
import org.hamersztein.klox.token.Token
import org.hamersztein.klox.token.TokenType.EOF
import org.hamersztein.klox.token.TokenType.NUMBER
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class ScannerCommentTest {

    @Test
    fun `should not add a token for a comment`() {
        assertProgram("// some comment") {
            assertEquals(1, it.size)
            assertEquals(EOF, it[0].type)
        }
    }

    @Test
    fun `should include correct line number for a token`() {
        val program = """
            // some comment
            1
        """.trimIndent()

        assertProgram(program) {
            assertEquals(2, it.size)

            assertEquals(2, it[0].line)

            assertEquals(EOF, it[1].type)
        }
    }

    @Test
    fun `should handle multiline comments`() {
        val program = """
            /*
            this is some multiline comment
            */
            1
        """.trimIndent()

        assertProgram(program) {
            assertEquals(2, it.size)

            val expectedToken = Token(type = NUMBER, lexeme = "1", literal = 1.0, line = 4)
            assertEquals(expectedToken, it[0])

            assertEquals(EOF, it[1].type)
        }
    }

}
