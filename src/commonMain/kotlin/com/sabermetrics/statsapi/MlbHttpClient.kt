package com.sabermetrics.statsapi

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.sabermetrics.statsapi.error.MlbStatsError
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Universal Multiplatform HTTP client for MLB Stats API with Arrow-driven functional error handling.
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

    suspend fun getJsonEither(url: String, params: Map<String, Any> = emptyMap()): Either<MlbStatsError, JsonElement> {
        return Either.catch {
            val response = client.get(url) {
                header("User-Agent", "mlb-statsapi-kmp/1.0.0 (Kotlin Multiplatform Functional; https://github.com/brentmzey/mlb-statsapi-kmp)")
                for ((key, value) in params) {
                    parameter(key, value.toString())
                }
            }
            val statusCode = response.status.value
            val statusText = response.status.description
            val text = response.bodyAsText()

            if (!response.status.isSuccess()) {
                return MlbStatsError.HttpError(
                    statusCode = statusCode,
                    statusText = statusText,
                    url = url,
                    responseBody = text.take(500)
                ).left()
            }
            text
        }.mapLeft { throwable ->
            MlbStatsError.NetworkError(
                message = "Failed to execute HTTP GET against $url: ${throwable.message}",
                url = url,
                causeMessage = throwable.stackTraceToString()
            )
        }.flatMap { rawText ->
            Either.catch {
                jsonParser.parseToJsonElement(rawText)
            }.mapLeft { parseEx ->
                MlbStatsError.ParsingError(
                    message = "Failed to parse JSON response from $url: ${parseEx.message}",
                    rawSnippet = rawText.take(300),
                    causeMessage = parseEx.stackTraceToString()
                )
            }
        }
    }

    suspend fun getJsonObjectEither(url: String, params: Map<String, Any> = emptyMap()): Either<MlbStatsError, JsonObject> {
        return getJsonEither(url, params).flatMap { elem ->
            (elem as? JsonObject)?.right()
                ?: MlbStatsError.ParsingError(
                    message = "Expected JSON Object root but received: ${elem::class.simpleName}",
                    rawSnippet = elem.toString().take(200)
                ).left()
        }
    }

    suspend fun getJson(url: String, params: Map<String, Any> = emptyMap()): JsonElement {
        return getJsonEither(url, params).fold(
            ifLeft = { throw RuntimeException(it.message) },
            ifRight = { it }
        )
    }

    suspend fun getJsonObject(url: String, params: Map<String, Any> = emptyMap()): JsonObject {
        return getJsonObjectEither(url, params).fold(
            ifLeft = { throw RuntimeException(it.message) },
            ifRight = { it }
        )
    }

    fun close() {
        client.close()
    }
}
