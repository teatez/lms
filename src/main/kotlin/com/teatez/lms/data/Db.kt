package com.teatez.lms.data

import kotlin.reflect.full.*

interface Db {
    val sp: ScriptProvider
    fun connect()
    fun <T> exec(s: Script): DbResponse<T, MrError>
}

interface ScriptProvider {
    fun createFor(p: Persistable): Script
    fun readFor(p: Persistable): Script
    fun updateFor(p: Persistable): Script
    fun deleteFor(p: Persistable): Script
    fun projectFor(p: Persistable): Script
}

interface Script {
    fun get(): String
    fun fill(p: Persistable)
}

class MrPersistor<T: Any>(val db: Db){
    private fun deconstruct(p: T): Persistable {
        val props = p::class.declaredMemberProperties
        val (ks,vs) = props.map {
            prop ->
                prop.name to prop.getter.call(p)
        }.unzip()
        return Persistable(ks, vs)
    }

    private fun classifyValue(v: Any): ValueContainer {
        when(v) {
            is String -> StringV(v)
            is Int -> IntV(v)
            is Long -> LongV(v)
            else -> BadValue()
        }
    }

    private var cs: Script? = null
    fun create(p: T): DbResponse<T, MrError> {
        handle(p, cs)
    }

    private var rs: Script? = null
    fun read(p: T): DbResponse<T, MrError> {
        handle(p, rs)
    }

    private fun handle(p: T, s: Script): DbResponse<T, MrError> {
        val d = deconstruct(p)
        if(s== null) {
            s = db.sp.createFor(d) 
        }
        s.fill(d)
        return db.exec<T>(cs)
    }
}

abstract ValueContainer {
    class ValuePointer(val k: String, val v: ValueContainer): ValueContainer()
    class Vc(val k: String, val v: Value): ValueContainer()
    class BadV: ValueContiner()
}

abstract class Value {
    inline class IntV(val v: Int): ValueContainer
    inline class StringV(val v: String): ValueContainer
    inline class LongV(val v: Long): ValueContainer

}

data class DbCreateError(override val code: String, override val message: String) : MrError
data class DbReadError(override val code: String, override val message: String) : MrError 
data class DbUpdateError(override val code: String, override val message: String) : MrError 
data class DbDeleteError(override val code: String, override val message: String) : MrError

interface DbResponse<S,F: MrError>
data class Success<S, F: MrError>(val value: S): DbResponse<S,F>
data class Failure<S, F: MrError>(val error: F): DbResponse<S,F>

interface MrError {
    val code: String
    val message: String
}

