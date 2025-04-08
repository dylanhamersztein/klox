package org.hamersztein.klox.util

import java.io.ByteArrayOutputStream
import java.io.PrintStream

object TestUtils {
    fun mockSystemOutStream() = doMock(System.out, System::setOut)

    fun mockSystemErrorStream() = doMock(System.err, System::setErr)

    private fun doMock(stream: PrintStream, set: (p: PrintStream) -> Unit): Pair<ByteArrayOutputStream, () -> Unit> {
        val captor = ByteArrayOutputStream()

        set(PrintStream(captor))

        return captor to { set(stream) }
    }
}