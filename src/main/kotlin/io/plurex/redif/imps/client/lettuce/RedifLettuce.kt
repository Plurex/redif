package io.plurex.redif.imps.client.lettuce

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import io.plurex.redif.RedifAPI
import io.plurex.redif.imps.client.RedifClientConfig
import kotlinx.coroutines.flow.toList

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class RedifLettuce(config: RedifClientConfig) : RedifAPI {

    private val client: RedisClient
    private val commands: RedisCoroutinesCommands<String, String>

    init {
        val uriBuilder = RedisURI.builder()
        uriBuilder.withHost(config.host)
        uriBuilder.withPort(config.port)
        config.auth?.let { uriBuilder.withAuthentication(it.user, it.password) }
        client = RedisClient.create(uriBuilder.build())
        commands = client.connect().coroutines()
    }

    override suspend fun pexpire(key: String, expireMillis: Long): Boolean {
        return commands.pexpire(key, expireMillis).check()
    }

    override suspend fun del(vararg keys: String): Long {
        var counter = 0L
        keys.forEach { pattern ->
            val matchingKeys = commands.keys(pattern).toList()
            if (matchingKeys.isNotEmpty()) {
                counter += commands.del(*matchingKeys.toTypedArray()).check()
            }
        }
        return counter
    }

    override suspend fun set(key: String, value: String) {
        commands.set(key, value)
    }

    override suspend fun psetx(key: String, value: String, expireMillis: Long) {
        commands.psetex(key, expireMillis, value)
    }

    override suspend fun setnx(key: String, value: String): Boolean {
        return commands.setnx(key, value).check()
    }

    override suspend fun get(key: String): String? {
        return commands.get(key)
    }
}

private fun <T : Any> T?.check(): T {
    return this ?: throw Exception("Null check failed on Lettuce coroutines api")
}
