package io.plurex.redif

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import io.plurex.pangolin.random.randString
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class KVStoreTest : KVStoreTestCase() {

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `set and get`(client: KVStoreAPI) = runBlocking {
        val key = randString()
        val value = randString()

        client.set(key, value)

        assertThat(client.get(key)).isEqualTo(value)
    }

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `set delete get`(client: KVStoreAPI) = runBlocking {
        val key = randString()
        val value = randString()
        val key2 = randString()
        val value2 = randString()

        client.set(key, value)
        client.set(key2, value2)

        val count = client.del(key, key2)
        assertThat(count).isEqualTo(2)

        assertThat(client.get(key)).isNull()
        assertThat(client.get(key2)).isNull()
    }

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `psetx and get`(client: KVStoreAPI) = runBlocking {
        val key = randString()
        val value = randString()

        client.psetx(key, value, 1000)

        assertThat(client.get(key)).isEqualTo(value)
    }

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `psetx and get - expired`(client: KVStoreAPI) = runBlocking {
        val key = randString()
        val value = randString()

        client.psetx(key, value, 10)

        delay(15)

        assertThat(client.get(key)).isNull()
    }


    @ParameterizedTest
    @MethodSource("provideClients")
    fun `pexpire - success`(client: KVStoreAPI) = runBlocking {
        val key = randString()
        val value = randString()

        client.psetx(key, value, 10)
        assertThat(client.pexpire(key, 20)).isTrue()
        delay(12)

        assertThat(client.get(key)).isEqualTo(value)

        delay(15)

        assertThat(client.get(key)).isNull()
    }

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `pexpire - does not exist`(client: KVStoreAPI) = runBlocking {
        assertThat(client.pexpire(randString(), 20)).isFalse()
    }

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `setnx - not set yet - get`(client: KVStoreAPI) = runBlocking {
        val key = randString()
        val value = randString()

        val result = client.setnx(key, value)

        assertThat(result).isTrue()

        assertThat(client.get(key)).isEqualTo(value)
    }

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `setnx - already set - get`(client: KVStoreAPI) = runBlocking {
        val key = randString()
        val value = randString()
        val original = randString()

        client.set(key, original)
        val result = client.setnx(key, value)

        assertThat(result).isFalse()

        assertThat(client.get(key)).isEqualTo(original)
    }

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `setnx - already set but expired - get`(client: KVStoreAPI) = runBlocking {
        val key = randString()
        val value = randString()
        val original = randString()

        client.set(key, original)
        client.pexpire(key, 10)
        delay(11)
        val result = client.setnx(key, value)

        assertThat(result).isTrue()

        assertThat(client.get(key)).isEqualTo(value)
    }

}