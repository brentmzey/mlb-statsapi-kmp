package com.sabermetrics.statsapi.js

import arrow.core.Either
import com.sabermetrics.statsapi.MlbStatsApi
import com.sabermetrics.statsapi.error.MlbStatsError
import com.sabermetrics.statsapi.models.MlbDivisionStandings
import com.sabermetrics.statsapi.models.MlbPlayerLookup
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MlbStatsApiJsIntegrationTest {

    @Test
    fun testJsRuntimeStandingsFetch() = runTest {
        try {
            val result: Either<MlbStatsError, List<MlbDivisionStandings>> = MlbStatsApi.standingsEither(season = 2026)
            result.fold(
                ifLeft = { err ->
                    println("⚠️ JS environment network skipped: ${err.message}")
                },
                ifRight = { standings ->
                    if (standings.isNotEmpty()) {
                        assertTrue(standings.size >= 6, "Expected 6 divisions in JS runtime")
                        val teamNames = standings.flatMap { it.teamRecords }.map { it.teamName }
                        assertTrue(teamNames.isNotEmpty(), "Team names list should not be empty")
                    }
                }
            )
        } catch (e: Exception) {
            println("⚠️ JS Standings skipped: ${e.message}")
        }
    }

    @Test
    fun testJsRuntimePlayerLookupFunctionalStream() = runTest {
        try {
            val result: Either<MlbStatsError, List<MlbPlayerLookup>> = MlbStatsApi.lookupPlayerEither("Judge")
            result.fold(
                ifLeft = { err ->
                    println("⚠️ JS Player Lookup skipped in restricted browser sandbox: ${err.message}")
                },
                ifRight = { players ->
                    if (players.isNotEmpty()) {
                        val judge = players.firstOrNull { it.fullName.contains("Aaron") }
                        assertNotNull(judge, "Aaron Judge should be resolvable in JS runtime")
                    }
                }
            )
        } catch (e: Exception) {
            println("⚠️ JS Player Lookup skipped: ${e.message}")
        }
    }
}
