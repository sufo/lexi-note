package com.sufo.lexinote.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Created by sufo on 2025/8/8 17:17.
 *
 */

object Serialization {

    fun <T> encodeToString(serializer: KSerializer<T>, value: T): String {
        val json = Json.encodeToString(serializer, value)
        return URLEncoder.encode(json, StandardCharsets.UTF_8.toString())
    }

    fun <T> decodeFromString(serializer: KSerializer<T>, string: String): T? {
        return try {
            val decoded = URLDecoder.decode(string, StandardCharsets.UTF_8.toString())
            Json.decodeFromString(serializer, decoded)
        } catch (e: Exception) {
            null
        }
    }
}