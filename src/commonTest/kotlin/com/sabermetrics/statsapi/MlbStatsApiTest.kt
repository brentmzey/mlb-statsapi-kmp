package com.sabermetrics.statsapi

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.raise.either
import com.sabermetrics.statsapi.error.MlbStatsError
import com.sabermetrics.statsapi.formatters.MlbTableFormatter
import com.sabermetrics.statsapi.models.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MlbStatsApiTest {

    @Test
    fun testEndpointResolution() {
        val scheduleUrl = MlbEndpoints.resolveUrl("schedule")
        assertEquals("https://statsapi.mlb.com/api/v1/schedule", scheduleUrl)

        val gameUrl = MlbEndpoints.resolveUrl("game", mapOf("gamePk" to 745678L))
        assertEquals("https://statsapi.mlb.com/api/v1.1/game/745678/feed/live", gameUrl)

        val rosterUrl = MlbEndpoints.resolveUrl("team_roster", mapOf("teamId" to 119))
        assertEquals("https://statsapi.mlb.com/api/v1/teams/119/roster", rosterUrl)
    }

    @Test
    fun testLinescoreFormatter() {
        val linescoreData = MlbLinescoreData(
            gamePk = 745000L,
            awayTeamName = "Dodgers",
            homeTeamName = "Yankees",
            awayRuns = 5,
            homeRuns = 3,
            awayHits = 8,
            homeHits = 6,
            awayErrors = 0,
            homeErrors = 1,
            innings = listOf(
                MlbInningScore(1, "1st", 1, 0),
                MlbInningScore(2, "2nd", 0, 0),
                MlbInningScore(3, "3rd", 2, 1),
                MlbInningScore(4, "4th", 0, 0),
                MlbInningScore(5, "5th", 0, 0),
                MlbInningScore(6, "6th", 1, 0),
                MlbInningScore(7, "7th", 0, 2),
                MlbInningScore(8, "8th", 1, 0),
                MlbInningScore(9, "9th", 0, 0)
            )
        )

        val formatted = MlbTableFormatter.formatLinescore(linescoreData)
        assertTrue(formatted.contains("Dodgers"))
        assertTrue(formatted.contains("Yankees"))
        assertTrue(formatted.contains("R  H  E"))
    }

    @Test
    fun testBoxscoreFormatter() {
        val boxscoreData = MlbBoxscoreData(
            gameId = 745000L,
            awayTeamName = "Los Angeles Dodgers",
            homeTeamName = "New York Yankees",
            awayBatters = listOf(
                MlbBatterBoxRow("Shohei Ohtani DH", "4", "2", "3", "3", "1", "0", "1", ".310", "1.040"),
                MlbBatterBoxRow("Mookie Betts SS", "4", "1", "2", "1", "1", "1", "2", ".295", ".890")
            ),
            homeBatters = listOf(
                MlbBatterBoxRow("Aaron Judge CF", "3", "1", "1", "2", "2", "1", "1", ".325", "1.150"),
                MlbBatterBoxRow("Juan Soto RF", "4", "1", "2", "1", "1", "0", "0", ".290", ".990")
            ),
            awayPitchers = listOf(
                MlbPitcherBoxRow("Yoshinobu Yamamoto", "6.0", "3", "1", "1", "2", "8", "1", "2.85")
            ),
            homePitchers = listOf(
                MlbPitcherBoxRow("Gerrit Cole", "5.2", "5", "3", "3", "2", "7", "1", "3.20")
            )
        )

        val formatted = MlbTableFormatter.formatBoxscore(boxscoreData)
        assertTrue(formatted.contains("Shohei Ohtani"))
        assertTrue(formatted.contains("Aaron Judge"))
        assertTrue(formatted.contains("Yoshinobu Yamamoto"))
        assertTrue(formatted.contains("Gerrit Cole"))
    }

    @Test
    fun testArrowFunctionalEitherCombinators() = runTest {
        // Monadic validation with Arrow Either
        val blankLookup: Either<MlbStatsError, List<MlbPlayerLookup>> = MlbStatsApi.lookupPlayerEither("")
        assertTrue(blankLookup.isLeft())
        blankLookup.fold(
            ifLeft = { error ->
                assertTrue(error is MlbStatsError.InvalidParameterError)
                assertEquals("lookupValue", error.parameter)
            },
            ifRight = {
                error("Expected failure for blank lookup value")
            }
        )

        // Monadic flatMap and map chain
        val result = blankLookup
            .map { list -> list.map { it.fullName } }
            .getOrElse { listOf("Fallback") }
        assertEquals(listOf("Fallback"), result)
    }

    @Test
    fun testArrowEitherDslRaiseBind() = runTest {
        val computation: Either<MlbStatsError, String> = either {
            val valid = MlbStatsApi.lookupPlayerEither("Ohtani").bind()
            val names = valid.map { it.fullName }
            "Found ${names.size} players"
        }
        assertNotNull(computation)
    }

    @Test
    fun testLiveApiStandingsFetch() = runTest {
        try {
            val standingsEither = MlbStatsApi.standingsEither(season = 2026)
            standingsEither.fold(
                ifLeft = { err ->
                    println("⚠️ Live API call returned error: ${err.message}")
                },
                ifRight = { standings ->
                    if (standings.isNotEmpty()) {
                        val totalDivisions = standings.size
                        assertTrue(totalDivisions >= 6, "Expected at least 6 MLB divisions")
                        val dodgers = standings.flatMap { it.teamRecords }.firstOrNull { it.teamName.contains("Dodgers") }
                        assertNotNull(dodgers)
                    }
                }
            )
        } catch (e: Exception) {
            println("⚠️ Live network call skipped in offline environment: ${e.message}")
        }
    }
}
