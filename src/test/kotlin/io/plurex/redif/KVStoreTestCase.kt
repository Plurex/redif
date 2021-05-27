package io.plurex.redif

import io.plurex.redif.imps.client.KVStoreClientConfig
import io.plurex.redif.imps.client.lettuce.KVStoreLettuce
import io.plurex.redif.imps.embedded.KVStoreInMemory
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class KVStoreTestCase {

    fun provideClients(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(KVStoreInMemory()),
            Arguments.of(
                KVStoreLettuce(
                    KVStoreClientConfig(
                        host = "localhost",
                        port = 6378
                    )
                )
            )
        )
    }

}