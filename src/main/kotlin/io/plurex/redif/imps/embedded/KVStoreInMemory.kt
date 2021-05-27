package io.plurex.redif.imps.embedded

import io.plurex.pangolin.actors.wrapper.ActorWrapper
import io.plurex.pangolin.time.timeNowMillis
import io.plurex.redif.KVStoreAPI


class KVStoreInMemory : KVStoreAPI, ActorWrapper() {

    private val keyValues: MutableMap<String, MetaValue> = mutableMapOf()

    override suspend fun set(key: String, value: String) = synchronized {
        keyValues[key] = MetaValue(
            value = value
        )
    }

    override suspend fun psetx(key: String, value: String, expireMillis: Long) = synchronized {
        keyValues[key] = MetaValue(
            value = value,
            expireTimestampMillis = timeNowMillis() + expireMillis
        )
    }

    override suspend fun setnx(key: String, value: String): Boolean = synchronized {
        val meta = get(key)
        if (meta == null) {
            keyValues[key] = MetaValue(
                value = value
            )
            true
        } else {
            false
        }
    }

    override suspend fun get(key: String): String? = synchronized {
        val meta = keyValues[key]
        if (meta != null) {
            val expire = meta.expireTimestampMillis
            if (expire != null && timeNowMillis() > expire) {
                keyValues.remove(key)
                null
            } else {
                meta.value
            }
        } else {
            null
        }
    }

    override suspend fun pexpire(key: String, expireMillis: Long): Boolean = synchronized {
        val meta = keyValues[key]
        if (meta != null) {
            meta.expireTimestampMillis = timeNowMillis() + expireMillis
            true
        } else {
            false
        }
    }

    override suspend fun del(vararg keys: String): Long = synchronized {
        var counter = 0L
        keys.forEach {
            keyValues.remove(it)?.let {
                counter++
            }
        }
        counter
    }
}

private data class MetaValue(
    val value: String,
    var expireTimestampMillis: Long? = null
)