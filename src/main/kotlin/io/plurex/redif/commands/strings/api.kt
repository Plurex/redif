package io.plurex.redif.commands.strings

interface StringsAPI {
    suspend fun set(key: String, value: String)
    suspend fun psetx(key: String, value: String, expireMillis: Long)
    suspend fun setnx(key: String, value: String): Boolean
    suspend fun get(key: String): String?
}