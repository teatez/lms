package com.teatez.lms.data

import org.junit.Assert.assertEquals
import org.junit.Test

class DbTest {
    data class Balls(val sacks: String, val shit: Int, var stuff: String): MPPersistable {
        override fun me(): String = "Balls"
        override fun fields(): Map<String, Any> = mapOf("sacks" to sacks, "shit" to shit, "stuff" to stuff)

    }
    class MockDb(val osp: ScriptProvider = MockScriptProvider()): Db {
        override val sp: ScriptProvider = osp
        override fun connect() = println("connect")
        override fun exec(s: Script): DbResponse = Success("yay")
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

    data class TestAddr(val city: String, val state: String, val zip: String, val street: String): MPPersistable {
        override fun me(): String = "TestAddr"
        override fun fields(): Map<String, Any> = mapOf("city" to city, "state" to state, "zip" to zip, "street" to street)
    }
    data class TestPerson(val name: String, val age: Int, val addr: TestAddr): MPPersistable {
        override fun me(): String = "TestPerson"
        override fun fields(): Map<String, Any> = mapOf("name" to name, "age" to age, "addr" to addr)
    }

    @Test fun deconstructorSimpleTest() {
        val tp = TestPerson("jimbo slimbo", 200, TestAddr("columbus", "ohio", "43202", "123 ligma ave"))

        val sp = MockScriptProvider()
        var target: ValueContainer? = null
        sp.createForFn = {vc -> target = vc; MockScript()}
        val mp = MrPersistor<TestPerson>(MockDb(sp))
        mp.create(tp)
        assertEquals(target, 
            ListContainer("TestPerson", listOf(
                Vc("name", StringV("jimbo slimbo")),
                Vc("age", IntV(200)),
                ListContainer("TestAddr", listOf(
                    Vc("city", StringV("columbus")), 
                    Vc("state", StringV("ohio")), 
                    Vc("zip", StringV("43202")),
                    Vc("street", StringV("123 ligma ave")))))))
    }

    data class TestInner(val moreJunk: String)
    data class TestOuter(val junk: String, val inside: TestInner): MPPersistable {
        override fun me(): String = "TestOuter"
        override fun fields(): Map<String, Any> = mapOf("junk" to junk, "inside" to inside)
    }
    @Test fun deconstructorErrorTest() {
        val subject = TestOuter("junk", TestInner("moreJunk"))

        val sp = MockScriptProvider()
        val mp = MrPersistor<TestOuter>(MockDb(sp))
        val result = mp.create(subject)
        assertEquals(Failure(listOf(DeconstructError("MP1","Bad Value Encountered: k=inside v=TestInner(moreJunk=moreJunk)"))), result)

    }
}
