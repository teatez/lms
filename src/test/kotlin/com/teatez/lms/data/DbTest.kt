package com.teatez.lms.data

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.reflect.*
import kotlin.reflect.full.*

class DbTest {
    data class Balls(val sacks: String, val shit: Int, var stuff: String): MPPersistable()

    class MockDb(val osp: ScriptProvider = MockScriptProvider()): Db {
        override val sp: ScriptProvider = osp
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
        override fun addValue(p: ValueContainer): Script = this
    }

    @Test fun createTest() {
        val sp = MockScriptProvider()
        var target: ValueContainer? = null
        sp.createForFn = {vc -> target = vc; MockScript()}
        val mp = MrPersistor<Balls>(MockDb(sp))
        mp.create(Balls("hehe",1,"haha"))
        assertEquals(target, 
            ListContainer("Balls", listOf(
                Vc("sacks", StringV("hehe")), 
                Vc("shit", IntV(1)), 
                Vc("stuff", StringV("haha")))))
    }

    data class TestAddr(val city: String, val state: String, val zip: String, val street: String)
    data class TestPerson(val name: String, val age: Int, val addr: TestAddr): MPPersistable()
    @Test fun deconstructorSimpleTest() {
        val tp = TestPerson("jimbo slimbo", 200, TestAddr("columbus", "ohio", "43202", "123 ligma ave"))

        val sp = MockScriptProvider()
        var target: ValueContainer? = null
        sp.createForFn = {vc -> target = vc; MockScript()}
        val mp = MrPersistor<TestPerson>(MockDb(sp))
        mp.create(tp)
        assertEquals(target, 
            ListContainer("TestPerson", listOf(
                ListContainer("TestAddr", listOf(
                    Vc("city", StringV("columbus")), 
                    Vc("state", StringV("ohio")), 
                    Vc("street", StringV("123 ligma ave")),
                    Vc("zip", StringV("43202")))),
                Vc("age", IntV(200)),
                Vc("name", StringV("jimbo slimbo"))
            )))
    }
}
