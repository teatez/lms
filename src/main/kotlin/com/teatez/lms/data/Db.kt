package com.teatez.lms.data

interface Db {
    val sp: ScriptProvider
    fun connect()
    fun exec(s: Script): DbResponse
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

