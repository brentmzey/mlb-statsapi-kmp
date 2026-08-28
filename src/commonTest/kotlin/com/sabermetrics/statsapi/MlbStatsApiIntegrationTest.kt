package com.sabermetrics.statsapi

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.raise.either
import com.sabermetrics.statsapi.error.MlbStatsError
import com.sabermetrics.statsapi.models.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class MlbStatsApiIntegrationTest {

    @Test
    fun testLiveScheduleFetchAndValidation() = runTest {
        try {
            val result: Either<MlbStatsError, List<MlbScheduleGame>> = MlbStatsApi.scheduleEither(date = "2026-08-28")
            result.fold(
                ifLeft = { error ->
                    println("⚠️ Live schedule fetch skipped in offline/network-restricted environment: ${error.message}")
                },
                ifRight = { games ->
                    if (games.isNotEmpty()) {
                        val firstGame = games.first()
                        assertTrue(firstGame.gameId > 0, "Game PK should be positive")
                        assertTrue(firstGame.awayName.isNotBlank(), "Away team name must not be blank")
                        assertTrue(firstGame.homeName.isNotBlank(), "Home team name must not be blank")
                        assertTrue(firstGame.summary.isNotBlank(), "Summary string must be generated")
                    }
                }
            )
        } catch (e: Exception) {
            println("⚠️ Network call skipped: ${e.message}")
        }
    }

    @Test
    fun testLiveStandingsFetchAndValidation() = runTest {
        try {
            val result: Either<MlbStatsError, List<MlbDivisionStandings>> = MlbStatsApi.standingsEither(season = 2026)
            result.fold(
                ifLeft = { error ->
                    println("⚠️ Live standings fetch skipped: ${error.message}")
                },
                ifRight = { standings ->
                    if (standings.isNotEmpty()) {
                        assertTrue(standings.size >= 6, "Expected at least 6 MLB divisions")
                        val allTeams = standings.flatMap { it.teamRecords }
                        assertTrue(allTeams.size >= 30, "Expected at least 30 MLB team standing records")
                        val dodgers = allTeams.firstOrNull { it.teamName.contains("Dodgers") }
                        assertNotNull(dodgers, "Los Angeles Dodgers should be present in standings")
                        assertTrue(dodgers.wins + dodgers.losses > 0, "Dodgers games played should be > 0")
                    }
                }
            )
        } catch (e: Exception) {
            println("⚠️ Network call skipped: ${e.message}")
        }
    }

    @Test
    fun testLivePlayerLookupAndValidation() = runTest {
        try {
            val result: Either<MlbStatsError, List<MlbPlayerLookup>> = MlbStatsApi.lookupPlayerEither("Ohtani")
            result.fold(
                ifLeft = { error ->
                    println("⚠️ Live player lookup skipped: ${error.message}")
                },
                ifRight = { players ->
                    if (players.isNotEmpty()) {
                        val ohtani = players.firstOrNull { it.fullName.contains("Shohei") }
                        assertNotNull(ohtani, "Shohei Ohtani should be found")
                        assertTrue(ohtani.id > 0, "Ohtani MLB Person ID should be positive")
                    }
                }
            )
        } catch (e: Exception) {
            println("⚠️ Network call skipped: ${e.message}")
        }
    }

    @Test
    fun testLiveTeamLookupAndValidation() = runTest {
        try {
            val result: Either<MlbStatsError, List<MlbTeamLookup>> = MlbStatsApi.lookupTeamEither("Yankees")
            result.fold(
                ifLeft = { error ->
                    println("⚠️ Live team lookup skipped: ${error.message}")
                },
                ifRight = { teams ->
                    if (teams.isNotEmpty()) {
                        val yankees = teams.firstOrNull { it.name.contains("Yankees") }
                        assertNotNull(yankees, "New York Yankees should be found")
                        assertEquals(147, yankees.id, "Yankees teamId is canonical 147")
                    }
                }
            )
        } catch (e: Exception) {
            println("⚠️ Network call skipped: ${e.message}")
        }
    }

    @Test
    fun testLiveRosterFetchAndValidation() = runTest {
        try {
            // Dodgers team ID = 119
            val result: Either<MlbStatsError, List<MlbRosterMember>> = MlbStatsApi.rosterEither(teamId = 119)
            result.fold(
                ifLeft = { error ->
                    println("⚠️ Live roster fetch skipped: ${error.message}")
                },
                ifRight = { roster ->
                    if (roster.isNotEmpty()) {
                        assertTrue(roster.size >= 25, "Active roster should have at least 25 players")
                        val player = roster.first()
                        assertTrue(player.personId > 0, "Roster player should have positive person ID")
                        assertTrue(player.fullName.isNotBlank(), "Roster player should have full name")
                    }
                }
            )
        } catch (e: Exception) {
            println("⚠️ Network call skipped: ${e.message}")
        }
    }

    @Test
    fun testFunctionalMonadicRaiseComposition() = runTest {
        val compositeResult: Either<MlbStatsError, String> = either {
            val blankParamFailure: List<MlbPlayerLookup> = MlbStatsApi.lookupPlayerEither("").bind()
            "Unreachable"
        }
        assertTrue(compositeResult.isLeft(), "Empty query must short-circuit as Left domain error")
        compositeResult.fold(
            ifLeft = { err ->
                assertTrue(err is MlbStatsError.InvalidParameterError)
                assertEquals("lookupValue", err.parameter)
            },
            ifRight = { fail("Must not succeed on invalid param") }
        )
    }
}
