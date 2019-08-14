package com.teatez.lms.data

import kotlin.reflect.full.*
import kotlin.reflect.*


interface Db {
    val sp: ScriptProvider
    fun connect()
    fun <T> exec(s: Script): DbResponse<T, MrError>
}

interface ScriptProvider {
    fun createFor(vc: ValueContainer): Script
    fun readFor(vc: ValueContainer): Script
    fun updateFor(vc: ValueContainer): Script
    fun deleteFor(vc: ValueContainer): Script
    fun projectFor(vc: ValueContainer): Script
}

interface Script {
    fun get(): String
    fun addValue(p: ValueContainer): Script
}

abstract class Persistable
class MrPersistor<T: Persistable>(val db: Db){
     
    private fun reflectTo(o: Any) = Triple(o::class.simpleName ?: "no name", o::class.declaredMemberProperties, o)
    private fun deconstruct(name: String, props: Collection<KProperty1<*, *>>, rcvr: Any): ValueContainer {
        val vcs = props.map {
            p ->
                val value = classifyValue(p.getter.call(rcvr))
                when (value) {
                    is ComplexV -> {
                        val (n, ps, r) = reflectTo(value.v)
                        deconstruct(n, ps, r)
                    }
                    else -> Vc(p.name, value)
                }
        }
        return ListContainer(name, vcs)
    }

    private fun classifyValue(v: Any?): Value {
        val v = when(v) {
            is Int     -> IntV(v)
            is Long    -> LongV(v)
            is Short   -> ShortV(v)
            is Byte    -> ByteV(v)
            is Float   -> FloatV(v)
            is Double  -> DoubleV(v)
            is String  -> StringV(v)
            is Boolean -> BoolV(v)
            null    -> NullV
            else -> ComplexV(v)
        }
        return v
    }

    private var cs: Script? = null
    private var rs: Script? = null
    private var us: Script? = null
    private var ds: Script? = null
    private var ps: Script? = null

    fun create(p: T): DbResponse<T, MrError> {
        val (name, props, reciever) = reflectTo(p)
        val d = deconstruct(name, props, reciever)
        val s = cs ?: db.sp.createFor(d)
        val fs = s.addValue(d)
        return db.exec(fs)
    }

    fun read(p: T): DbResponse<T, MrError> {
        val (name, props, reciever) = reflectTo(p)
        val d = deconstruct(name, props, reciever)
        val s = rs ?: db.sp.readFor(d)
        val fs = s.addValue(d)
        return db.exec(fs)
    }

    fun update(p: T): DbResponse<T, MrError> {
        val (name, props, reciever) = reflectTo(p)
        val d = deconstruct(name, props, reciever)
        val s = us ?: db.sp.updateFor(d)
        val fs = s.addValue(d)
        return db.exec(fs)
    }

    fun delete(p: T): DbResponse<T, MrError> {
        val (name, props, reciever) = reflectTo(p)
        val d = deconstruct(name, props, reciever)
        val s = ds ?: db.sp.deleteFor(d)
        val fs = s.addValue(d)
        return db.exec(fs)
    }

    fun project(p: T): DbResponse<T, MrError> {
        val (name, props, reciever) = reflectTo(p)
        val d = deconstruct(name, props, reciever)
        val s = ps ?: db.sp.projectFor(d)
        val fs = s.addValue(d)
        return db.exec(fs)
    }
}

sealed class ValueContainer 
data class ValuePointer(val k: String, val v: ValueContainer): ValueContainer()
data class Vc(val k: String, val v: Value): ValueContainer()
data class ListContainer(val k: String, val v: List<ValueContainer?>): ValueContainer()
object BadV: ValueContainer()

sealed class Value
//numbericons
data class IntV(val v: Int): Value()
data class LongV(val v: Long): Value()
data class ShortV(val v: Short): Value()
data class ByteV(val v: Byte): Value()
data class FloatV(val v: Float): Value()
data class DoubleV(val v: Double): Value()

//water t
data class StringV(val v: String): Value()

//other stuff
data class BoolV(val v: Boolean): Value()
data class ComplexV(val v: Any): Value()
object NullV: Value()

sealed class MrError {
    abstract val code: String
    abstract val message: String
}
data class DbCreateError(override val code: String, override val message: String) : MrError()
data class DbReadError(override val code: String, override val message: String) : MrError()
data class DbUpdateError(override val code: String, override val message: String) : MrError() 
data class DbDeleteError(override val code: String, override val message: String) : MrError()
data class ScriptFillError(override val code: String, override val message: String) : MrError()
data class DeconstructError(override val code: String, override val message: String) : MrError()

sealed class DbResponse<out S,out F>
data class Success<S>(val value: S): DbResponse<S,Nothing>()
data class Failure(val error: MrError): DbResponse<Nothing,MrError>()

