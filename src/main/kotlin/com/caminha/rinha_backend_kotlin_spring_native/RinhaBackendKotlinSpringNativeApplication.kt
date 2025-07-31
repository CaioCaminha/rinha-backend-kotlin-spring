package com.caminha.rinha_backend_kotlin_spring_native

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@SpringBootApplication
@EnableWebFlux
class RinhaBackendKotlinSpringNativeApplication

fun main(args: Array<String>) {
	runApplication<RinhaBackendKotlinSpringNativeApplication>(*args)
}
