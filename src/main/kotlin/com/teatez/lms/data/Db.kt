package com.teatez.lms.data

import kotlin.reflect.full.*

interface Db {
    val sp: ScriptProvider
    fun connect()
    fun <T> exec(s: Script): DbResponse<T, MrError>
}

data class Persistable(val ks: List<String>, val vs: List<Any?>)

interface ScriptProvider {
    fun createFor(p: Persistable): Script
    fun readFor(p: Persistable): Script
    fun updateFor(p: Persistable): Script
    fun deleteFor(p: Persistable): Script
    fun projectFor(p: Persistable): Script
}

interface Script {
    fun get(): String
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
    fun create(p: T): DbResponse<T, MrError> {
        val d = deconstruct(p)
        val cs = db.sp.createFor(d) 
        return db.exec<T>(cs)
    }
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

