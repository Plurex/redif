package io.plurex.redif

import io.plurex.redif.imps.client.RedifClientConfig
import io.plurex.redif.imps.client.lettuce.RedifLettuce
import io.plurex.redif.imps.embedded.RedifInMemory
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class RedifTestCase {

    fun provideClients(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(RedifInMemory()),
            Arguments.of(
                RedifLettuce(
                    RedifClientConfig(
                        host = "localhost",
                        port = 6377
                    )
                )
            )
        )
    }

}