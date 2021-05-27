package io.plurex.redif.commands.keys

interface KeysAPI {
    suspend fun pexpire(key: String, expireMillis: Long): Boolean
    suspend fun del(vararg keys: String): Long
}