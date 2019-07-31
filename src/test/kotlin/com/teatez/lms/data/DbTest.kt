package com.teatez.lms.data

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.reflect.*
import kotlin.reflect.full.*

class DbTest {
    data class Balls(val sacks: String, val shit: Int, var stuff: String): Persistable()

    class MockDb: Db {
        override val sp: ScriptProvider = MockScriptProvider()
        override fun connect() = println("connect")
        override fun <T> exec(s: Script): DbResponse<T, MrError> = Success("yay" as T)
    }

    class MockScriptProvider: ScriptProvider {
        var createForFn: (ValueContainer)->Script =  {vc: ValueContainer -> println(vc); MockScript()}
        override fun createFor(vc: ValueContainer): Script = createForFn(vc)
        override fun readFor(vc: ValueContainer): Script = MockScript()
        override fun updateFor(vc: ValueContainer): Script = MockScript()
        override fun deleteFor(vc: ValueContainer): Script = MockScript()
        override fun projectFor(vc: ValueContainer): Script = MockScript()
    }

    class MockScript: Script {
        override fun get(): String = "script"
        override fun fill(p: ValueContainer): Script = this
    }

    @Test fun createTest() {
        val mp = MrPersistor<Balls>(MockDb())
        mp.create(Balls("hehe",1,"haha"))
    }
}
