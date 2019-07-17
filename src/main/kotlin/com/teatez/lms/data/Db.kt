package com.teatez.lms.data

import kotlin.reflect.full.*

interface Db {
    val sp: ScriptProvider
    fun connect()
    fun <T> exec(s: Script): DbResponse<T, MrError>
}

interface ScriptProvider {
    fun createFor(p: ValueContainer): Script
    fun readFor(p: ValueContainer): Script
    fun updateFor(p: ValueContainer): Script
    fun deleteFor(p: ValueContainer): Script
    fun projectFor(p: ValueContainer): Script
}

interface Script {
    fun get(): String
    fun fill(p: ValueContainer)
}

class MrPersistor<T: Any>(val db: Db){
    private fun deconstruct(obj: T): ValueContainer {
        val name = obj::class.name
        val props = obj::class.declaredMemberProperties
        val vs = props.map {prop -> d(prop)}
        return ValuePointer(name, vs)
    }

    private tailrec fun d(prop: KProperty1): ValueContainer {
        val v = classifyValue(prop.getter.call(obj))
        if (v is ComplexValue) ValuePointer(prop.name, d(v))
        else Vc(prop.name, v)
    }

    private fun classifyValue(v: Any): Value {
        when(v) {
            is Int -> IntV(v)
            is Long -> LongV(v)
            is Short -> ShortV(v)
            is Byte -> ByteV(v)
            is Float -> FloatV(v)
            is Double -> DoubleV(v)
            is String -> StringV(v)
            is Boolean -> BoolV(v)
            else -> ComplexValue()
        }
    }

    companion object MisterPersistor {
        private var cs: Script? = null
        private var rs: Script? = null
        private var us: Script? = null
        private var ds: Script? = null
        private var ps: Script? = null

        fun create(p: T): DbResponse<T, MrError> {
            val d = deconstruct(p)
            if(cs == null) cs = db.createFor(d)
            db.exec(cs.fill(d))
        }

        fun read(p: T): DbResponse<T, MrError> {
            val d = deconstruct(p)
            if(rs == null) rs = db.readFor(d)
            db.exec(cs.fill(d))
        }

        fun update(p: T): DbResponse<T, MrError> {
            val d = deconstruct(p)
            if(us == null) us = db.updateFor(d)
            db.exec(us.fill(d))
        }

        fun delete(p: T): DbResponse<T, MrError> {
            val d = deconstruct(p)
            if(ds == null) ds = db.deleteFor(d)
            db.exec(ds.fill(d))
        }

        fun project(p: T): DbResponse<T, MrError> {
            val d = deconstruct(p)
            if(ps == null) ps = db.projectFor(d)
            db.exec(ps.fill(d)
        }
    }
}

abstract ValueContainer {
    class ValuePointer(val k: String, val v: List[ValueContainer]): ValueContainer()
    class Vc(val k: String, val v: Value): ValueContainer()
    class BadV: ValueContiner()
}

abstract class Value {
    //numbericons
    inline class IntV(val v: Int): Value
    inline class LongV(val v: Long): Value
    inline class ShortV(val v: Short): Value
    inline class ByteV(val v: Byte): Value
    inline class FloatV(val v: Float): Value
    inline class DoubleV(val v: Double): Value

    //water t
    inline class StringV(val v: String): Value

    //other stuff
    inline class BoolV(val v: Boolean): Value
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

