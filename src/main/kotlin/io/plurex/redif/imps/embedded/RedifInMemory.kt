package io.plurex.redif.imps.embedded

import io.plurex.redif.RedifAPI
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RedifInMemory(private val clock: Clock = Clock.System) : RedifAPI {

    private val mutex = Mutex()
    private val keyValues: MutableMap<String, MetaValue> = mutableMapOf()

    override suspend fun set(key: String, value: String) =
        mutex.withLock { keyValues[key] = MetaValue(value = value) }

    override suspend fun psetx(key: String, value: String, expireMillis: Long) =
        mutex.withLock {
            keyValues[key] =
                MetaValue(value = value, expireAt = clock.now() + expireMillis.milliseconds)
        }

    override suspend fun setnx(key: String, value: String): Boolean =
        mutex.withLock {
            val meta = getLocked(key)
            if (meta == null) {
                keyValues[key] = MetaValue(value = value)
                true
            } else {
                false
            }
        }

    override suspend fun get(key: String): String? = mutex.withLock { getLocked(key)?.value }

    private fun getLocked(key: String): MetaValue? {
        val meta = keyValues[key]
        return if (meta != null) {
            val expireAt = meta.expireAt
            if (expireAt != null && expireAt <= clock.now()) {
                keyValues.remove(key)
                null
            } else {
                meta
            }
        } else {
            null
        }
    }

    override suspend fun pexpire(key: String, expireMillis: Long): Boolean =
        mutex.withLock {
            val meta = getLocked(key)
            if (meta != null) {
                meta.expireAt = clock.now() + expireMillis.milliseconds
                true
            } else {
                false
            }
        }

    override suspend fun del(vararg keys: String): Long =
        mutex.withLock {
            var counter = 0L
            val allKeys = keyValues.keys.toList()
            keys.forEach { pattern ->
                val regex = globToRegex(pattern)
                allKeys.forEach { key ->
                    if (regex.matches(key)) {
                        keyValues.remove(key)?.let { counter++ }
                    }
                }
            }
            counter
        }
}

private fun globToRegex(pattern: String): Regex {
    val regexStr = buildString {
        var i = 0
        while (i < pattern.length) {
            val char = pattern[i]
            when (char) {
                '*' -> append(".*")
                '?' -> append(".")
                '[' -> {
                    append("[")
                    i++
                    if (i < pattern.length && pattern[i] == '^') {
                        append("^")
                        i++
                    }
                    while (i < pattern.length && pattern[i] != ']') {
                        if (pattern[i] == '\\' && i + 1 < pattern.length) {
                            append("\\")
                            i++
                        }
                        append(pattern[i])
                        i++
                    }
                    append("]")
                }

                '.',
                '(',
                ')',
                '+',
                '{',
                '}',
                '|',
                '^',
                '$',
                '\\' -> {
                    append("\\")
                    append(char)
                }

                else -> append(char)
            }
            i++
        }
    }
    return Regex("^$regexStr$")
}

private data class MetaValue(val value: String, var expireAt: Instant? = null)
