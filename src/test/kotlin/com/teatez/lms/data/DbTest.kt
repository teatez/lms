package com.teatez.lms.data

import org.junit.Assert.assertEquals
import org.junit.Test

class DbTest {
    data class TestClass(just: String, testing: Int, deconstruction: String)

    class DbMock(override val sp: ScriptProvider, execFn: (s:Script) -> Any?): Db {
        override fun connect() {/*do nothing*/}
        override fun <T> exec(s: Script): DbResponse<T, MrError> = execFn(s)
    }

    class ScriptProviderMock: ScriptProvider (
        val cfFn: (Persistable) -> Script = (p) -> ScriptMock()
        val rfFn: (Persistable) -> Script = (p) -> ScriptMock()
        val ufFn: (Persistable) -> Script = (p) -> ScriptMock()
        val dfFn: (Persistable) -> Script = (p) -> ScriptMock()
        val pfFn: (Persistable) -> Script = (p) -> ScriptMock()
    ){
        override fun createFor(p: Persistable): Script = cfFn(p)
        override fun readFor(p: Persistable): Script = rfFn(p)
        override fun updateFor(p: Persistable): Script = ufFn(p)
        override fun deleteFor(p: Persistable): Script = dfFn(p)
        override fun projectFor(p: Persistable): Script = pfFn(p)
    }

    class ScriptMock(val s: String = ""): Script {
        override fun get(): String = s
    }
    
    @Test fun createTest() {
        assertEquals(MyBalls().sugma(s), b)
    }
}
