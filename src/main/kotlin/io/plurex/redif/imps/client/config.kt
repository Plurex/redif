package io.plurex.redif.imps.client

data class RedifClientConfig(
    val host: String,
    val port: Int,
    val auth: RedifClientAuthConfig? = null
)

data class RedifClientAuthConfig(
    val user: String,
    val password: String
)