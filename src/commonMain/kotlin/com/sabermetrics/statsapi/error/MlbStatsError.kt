package com.sabermetrics.statsapi.error

import kotlinx.serialization.Serializable

/**
 * Strongly-typed algebraic error hierarchy for functional error handling in MLB-StatsAPI KMP.
 * Models all failure modes as explicit domain types with Arrow `Either<MlbStatsError, T>`.
 */
@Serializable
sealed interface MlbStatsError {
    val message: String

    @Serializable
    data class NetworkError(
        override val message: String,
        val url: String,
        val causeMessage: String? = null
    ) : MlbStatsError

    @Serializable
    data class HttpError(
        val statusCode: Int,
        val statusText: String,
        val url: String,
        override val message: String = "HTTP $statusCode $statusText from $url",
        val responseBody: String = ""
    ) : MlbStatsError

    @Serializable
    data class ParsingError(
        override val message: String,
        val rawSnippet: String = "",
        val causeMessage: String? = null
    ) : MlbStatsError

    @Serializable
    data class EntityNotFoundError(
        val entityType: String,
        val identifier: String,
        override val message: String = "$entityType with identifier '$identifier' was not found."
    ) : MlbStatsError

    @Serializable
    data class InvalidParameterError(
        val parameter: String,
        val reason: String,
        override val message: String = "Invalid parameter '$parameter': $reason"
    ) : MlbStatsError

    @Serializable
    data class UpstreamApiError(
        override val message: String,
        val errorCode: String? = null
    ) : MlbStatsError
}
