package io.plurex.redif

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.isEmpty
import assertk.assertions.containsOnly
import java.util.*
import kotlin.math.absoluteValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class RedifTest : RedifTestCase() {

    private val random = Random()

    private fun randString() = "GENERATED${random.nextInt().absoluteValue}"

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `set and get`(client: RedifAPI) = runBlocking {
        val key = randString()
        val value = randString()

        client.set(key, value)

        assertThat(client.get(key)).isEqualTo(value)
    }

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `set delete get`(client: RedifAPI) = runBlocking {
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
    fun `psetx and get`(client: RedifAPI) = runBlocking {
        val key = randString()
        val value = randString()

        client.psetx(key, value, 1000)

        assertThat(client.get(key)).isEqualTo(value)
    }

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `psetx and get - expired`(client: RedifAPI) = runBlocking {
        val key = randString()
        val value = randString()

        client.psetx(key, value, 10)

        delay(15)

        assertThat(client.get(key)).isNull()
    }

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `pexpire - success`(client: RedifAPI) = runBlocking {
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
    fun `pexpire - does not exist`(client: RedifAPI) = runBlocking {
        assertThat(client.pexpire(randString(), 20)).isFalse()
    }

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `setnx - not set yet - get`(client: RedifAPI) = runBlocking {
        val key = randString()
        val value = randString()

        val result = client.setnx(key, value)

        assertThat(result).isTrue()

        assertThat(client.get(key)).isEqualTo(value)
    }

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `setnx - already set - get`(client: RedifAPI) = runBlocking {
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
    fun `setnx - already set but expired - get`(client: RedifAPI) = runBlocking {
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

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `del with glob - asterisk`(client: RedifAPI) = runBlocking {
        client.set("user:1", "one")
        client.set("user:100", "hundred")
        client.set("other:1", "other")

        val count = client.del("user:*")
        assertThat(count).isEqualTo(2)

        assertThat(client.get("user:1")).isNull()
        assertThat(client.get("user:100")).isNull()
        assertThat(client.get("other:1")).isEqualTo("other")
        client.del("other:1")
    }

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `del with glob - question mark`(client: RedifAPI) = runBlocking {
        client.set("hello", "1")
        client.set("hallo", "2")
        client.set("hulloa", "3")

        val count = client.del("h?llo")
        assertThat(count).isEqualTo(2)

        assertThat(client.get("hello")).isNull()
        assertThat(client.get("hallo")).isNull()
        assertThat(client.get("hulloa")).isEqualTo("3")
        client.del("hulloa")
    }

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `del with glob - brackets`(client: RedifAPI) = runBlocking {
        client.set("user:1", "1")
        client.set("user:2", "2")
        client.set("user:3", "3")

        val count = client.del("user:[12]")
        assertThat(count).isEqualTo(2)

        assertThat(client.get("user:1")).isNull()
        assertThat(client.get("user:2")).isNull()
        assertThat(client.get("user:3")).isEqualTo("3")
        client.del("user:3")
    }

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `del with glob - range`(client: RedifAPI) = runBlocking {
        client.set("user:0", "0")
        client.set("user:5", "5")
        client.set("user:9", "9")
        client.set("user:a", "a")

        val count = client.del("user:[0-9]")
        assertThat(count).isEqualTo(3)

        assertThat(client.get("user:0")).isNull()
        assertThat(client.get("user:5")).isNull()
        assertThat(client.get("user:9")).isNull()
        assertThat(client.get("user:a")).isEqualTo("a")
        client.del("user:a")
    }

    @ParameterizedTest
    @MethodSource("provideClients")
    fun `del with glob - negation`(client: RedifAPI) = runBlocking {
        client.set("hallo", "1")
        client.set("hello", "2")

        val count = client.del("h[^e]llo")
        assertThat(count).isEqualTo(1)

        assertThat(client.get("hallo")).isNull()
        assertThat(client.get("hello")).isEqualTo("2")
        client.del("hello")
    }
}
