package com.caminha.rinha_backend_kotlin_spring_native.application.gateway.webclient

import com.caminha.rinha_backend_kotlin_spring_native.utils.KotlinSerializationJsonParser
import java.time.Duration
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.reactivestreams.Publisher
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.KotlinSerializationJsonDecoder
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

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






    @Bean
    fun webClient(): WebClient {
        // todo: Add a filter to log the request and possible errors
        return webClientBuilder.clone()
            .filter(
                ExchangeFilterFunction {request, next ->
                    mono {
                        next.exchange(request).awaitSingleOrNull()?.run {
                            val body = bodyToMono<String>().awaitSingleOrNull()
                            println("Status code: ${statusCode()}")
                            println("Response body: $body")

                            body?.let { mutate().body(body).build() } ?: this
                        }
                    }
                }
            )
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

