package org.hamersztein.klox.scanner

import org.hamersztein.klox.token.Token
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
object ScannerTestUtils {

    fun assertProgram(program: String, assertFunction: (List<Token>) -> Unit) =
        assertFunction(Scanner(program).scanTokens())

}
