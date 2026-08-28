package com.sabermetrics.statsapi

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Universal Multiplatform HTTP client for MLB Stats API with automatic serialization and resilience.
 */
class MlbHttpClient(
    private val client: HttpClient = createDefaultHttpClient()
) {
    companion object {
        val jsonParser: Json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
            encodeDefaults = true
        }

        fun createDefaultHttpClient(): HttpClient {
            return HttpClient {
                install(ContentNegotiation) {
                    json(jsonParser)
                }
            }
        }
    }

    suspend fun getJson(url: String, params: Map<String, Any> = emptyMap()): JsonElement {
        val response = client.get(url) {
            header("User-Agent", "mlb-statsapi-kmp/1.0.0 (Kotlin Multiplatform; https://github.com/brentmzey/mlb-statsapi-kmp)")
            for ((key, value) in params) {
                parameter(key, value.toString())
            }
        }
        val text = response.bodyAsText()
        return jsonParser.parseToJsonElement(text)
    }

    suspend fun getJsonObject(url: String, params: Map<String, Any> = emptyMap()): JsonObject {
        val element = getJson(url, params)
        return element as? JsonObject ?: JsonObject(emptyMap())
    }

    fun close() {
        client.close()
    }
}
