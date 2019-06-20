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
    fun read(query: T): DbResponse<List<T>, DbReadError>
    fun update(query: T): DbResponse<T, DbUpdateError>
    fun delete(id: T): DbResponse<Boolean, DbDeleteError>
}

data class DbCreateError(override val code: String, override val message: String) : DbError
data class DbReadError(override val code: String, override val message: String) : DbError 
data class DbUpdateError(override val code: String, override val message: String) : DbError 
data class DbDeleteError(override val code: String, override val message: String) : DbError

interface DbResponse<S,F: DbError>
data class Success<S, F: DbError>(val value: S): DbResponse<S,F>
data class Failure<S, F: DbError>(val error: F): DbResponse<S,F>

interface DbError {
    val code: String
    val message: String
}
data class Butthole(val shit: String): Persistable
