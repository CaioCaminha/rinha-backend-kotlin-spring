package com.caminha.rinha_backend_kotlin_spring_native.utils

import kotlinx.serialization.encodeToString


inline fun <reified T> T.toJsonString(): String {
    return KotlinSerializationJsonParser
        .DEFAULT_KOTLIN_SERIALIZATION_PARSER.encodeToString(this)
}