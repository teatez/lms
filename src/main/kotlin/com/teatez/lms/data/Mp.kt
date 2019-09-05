package com.teatez.lms.data

import kotlin.reflect.full.*
import kotlin.reflect.*

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

abstract class MPPersistable
class MrPersistor<T: MPPersistable>(val db: Db){
     
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
                    is BadV -> BadVc(p.name, value.v)
                    else -> Vc(p.name, value)
                }
        }
        return ListContainer(name, vcs)
    }

    private fun classifyValue(v: Any?): Value {
        val value = when(v) {
            is Int     -> IntV(v)
            is Long    -> LongV(v)
            is Short   -> ShortV(v)
            is Byte    -> ByteV(v)
            is Float   -> FloatV(v)
            is Double  -> DoubleV(v)
            is String  -> StringV(v)
            is Boolean -> BoolV(v)
            is MPPersistable -> ComplexV(v)
            null    -> NullV
            else -> BadV(v)
        }
        return value
    }

    fun perform(p: T, getScript: (Db,ValueContainer) -> Script): DbResponse<T, MrError> {
        val (name, props, reciever) = reflectTo(p)
        val valueContainer = deconstruct(name, props, reciever)
        val emptyScript = getScript(db, valueContainer)
        val scriptWithValues = emptyScript.addValue(valueContainer)
        return db.exec(scriptWithValues)
    }

    fun create(p: T): DbResponse<T, MrError> {
        return perform(p, ::getCreateScript)
    }

    fun read(p: T): DbResponse<T, MrError> {
        return perform(p, ::getReadScript)
    }

    fun update(p: T): DbResponse<T, MrError> {
        return perform(p, ::getUpdateScript)
    }

    fun delete(p: T): DbResponse<T, MrError> {
        return perform(p, ::getDeleteScript)
    }

    fun project(p: T): DbResponse<T, MrError> {
        return perform(p, ::getProjectScript)
    }

    companion object Mp {
        private val cs: ConcurrentMap<String, Script> = ConcurrentHashMap<String, Script>()
        private val rs: ConcurrentMap<String, Script> = ConcurrentHashMap<String, Script>()
        private val us: ConcurrentMap<String, Script> = ConcurrentHashMap<String, Script>()
        private val ds: ConcurrentMap<String, Script> = ConcurrentHashMap<String, Script>()
        private val ps: ConcurrentMap<String, Script> = ConcurrentHashMap<String, Script>()

        fun getCreateScript(db: Db, vc: ValueContainer): Script = cs.getOrPut(vc.k, {db.sp.createFor(vc)})
        fun getReadScript(db: Db, vc: ValueContainer): Script = rs.getOrPut(vc.k, {db.sp.readFor(vc)}) 
        fun getUpdateScript(db: Db, vc: ValueContainer): Script = us.getOrPut(vc.k, {db.sp.updateFor(vc)})
        fun getDeleteScript(db: Db, vc: ValueContainer): Script = ds.getOrPut(vc.k, {db.sp.deleteFor(vc)})
        fun getProjectScript(db: Db, vc: ValueContainer): Script = ps.getOrPut(vc.k, {db.sp.projectFor(vc)})
    }
}

object MpUtils {
    fun reduce2Errors(vc: ValueContainer): List<MrError> {
        return when(vc) {
            is ListContainer -> vc.v.flatMap {value -> reduce2Errors(value)}
            is BadVc -> listOf(DeconstructError("MP1", "Bad Value Encountered: k=${vc.k} v=${vc.v}"))
            is Vc -> emptyList()
        }
    }
}

sealed class ValueContainer {
    abstract val k: String
}
data class Vc(override val k: String, val v: Value): ValueContainer()
data class ListContainer(override val k: String, val v: List<ValueContainer>): ValueContainer()
data class BadVc(override val k: String, val v: Any): ValueContainer()

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
data class ListV(val v: List<*>): Value()
data class SetV(val v: Set<*>): Value()
data class BadV(val v: Any): Value()
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

