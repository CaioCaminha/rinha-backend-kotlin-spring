package com.caminha.rinha_backend_kotlin_spring_native.application.gateway.webclient

import com.caminha.rinha_backend_kotlin_spring_native.utils.KotlinSerializationJsonParser
import java.time.Duration
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.KotlinSerializationJsonDecoder
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider

@Configuration
class WebClientConfiguration(
    @Value("\${webclient.pool.max-connections}")
    val maxConnections: Int,
    @Value("\${webclient.pool.acquire-timeout}")
    val acquireTimeout: Long,
    private val webClientBuilder: WebClient.Builder
) {

    companion object {
        /**
         * Results in 1MB of max response memory usage
         *
         * 1024 bytes -> Kilobyte
         * 1024 kilobytes -> Megabyte
         *
         * Memory used for In-Memory response Buffering - Storing the http response body in memory before processing
         * JSON Parsing - Decoding / Encoding
         * Buffer chunks of data during network transfer
         *
         * Note: Spring boot uses 8MB by default - I'm trying to reduce memory usage as much as possible
         */
        private const val maxMemory = 1 * 1024 * 1024
    }




    val httpClient = HttpClient.create(
        /**
         * todo
         * I know it's creating a connection pool but how is this pool managed?
         * Understand how this ConnectionProvider works and how HttpClient manages the connection pool
         */
        ConnectionProvider.builder("http-connection-pool-payemnts-processor")
            .maxConnections(maxConnections)
            .pendingAcquireTimeout(Duration.ofMillis(acquireTimeout))
            .build()
    )

    @Bean
    fun webClient(): WebClient {
        return webClientBuilder.clone()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .codecs {
                it.defaultCodecs().maxInMemorySize((maxMemory * 1.2).toInt())
                it.defaultCodecs().apply {
                    kotlinSerializationJsonDecoder(
                        KotlinSerializationJsonDecoder(
                            KotlinSerializationJsonParser.DEFAULT_KOTLIN_SERIALIZATION_PARSER
                        )
                    )
                }
            }
            .build()
    }


}