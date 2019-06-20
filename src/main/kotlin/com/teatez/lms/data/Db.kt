package com.teatez.lms.data

interface Db {
    fun connect()
    fun exec(s: Script)
}

interface Script {
    fun get(): String
}

interface Persistable 
interface Persistor<T: Persistable>{
    val db: Db
    fun create(x: T): DbResponse<T, DbCreateError>
    fun read(query: T): List<T>
    fun update(query: T): T
    fun delete(id: T): Boolean
}
data class DbCreateError(val c: String, val m: String) : DbError {
    override fun getCode(): String = c
    override fun getMessage(): String = m
}

interface DbResponse<S: Persistable,F: DbError>
data class Success<S: Persistable, F: DbError>(val value: S): DbResponse<S,F>
data class Failure<S: Persistable, F: DbError>(val error: F): DbResponse<S,F>

interface DbError {
    fun getCode(): String
    fun getMessage(): String
}
data class Butthole(val shit: String): Persistable
