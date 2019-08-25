package com.teatez.lms.data

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

