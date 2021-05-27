package io.plurex.redif.imps.client


data class KVStoreClientConfig(
    val host: String,
    val port: Int,
    val auth: KVStoreClientAuthConfig? = null
)

data class KVStoreClientAuthConfig(
    val user: String,
    val password: String
)