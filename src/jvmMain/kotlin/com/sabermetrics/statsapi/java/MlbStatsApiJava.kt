package com.sabermetrics.statsapi.java

import arrow.core.Either
import com.sabermetrics.statsapi.MlbStatsApi
import com.sabermetrics.statsapi.error.MlbStatsError
import com.sabermetrics.statsapi.models.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import kotlinx.coroutines.runBlocking
import java.util.Optional
import java.util.concurrent.CompletableFuture

/**
 * Java 17+ Functional Interoperability Adapter for MLB-StatsAPI KMP.
 * Supports `Optional.ofNullable`, `Either<MlbStatsError, T>`, and `CompletableFuture<T>`.
 */
object MlbStatsApiJava {

    // =========================================================================
    // 1. Synchronous Methods returning Optional / Result
    // =========================================================================

    @JvmStatic
    fun schedule(date: String): List<MlbScheduleGame> = runBlocking {
        MlbStatsApi.schedule(date = date)
    }

    @JvmStatic
    fun scheduleEither(date: String): Either<MlbStatsError, List<MlbScheduleGame>> = runBlocking {
        MlbStatsApi.scheduleEither(date = date)
    }

    @JvmStatic
    fun standings(season: Int): List<MlbDivisionStandings> = runBlocking {
        MlbStatsApi.standings(season = season)
    }

    @JvmStatic
    fun standingsEither(season: Int): Either<MlbStatsError, List<MlbDivisionStandings>> = runBlocking {
        MlbStatsApi.standingsEither(season = season)
    }

    @JvmStatic
    fun boxscore(gamePk: Long): String = runBlocking {
        MlbStatsApi.boxscore(gamePk = gamePk)
    }

    @JvmStatic
    fun boxscoreData(gamePk: Long): Optional<MlbBoxscoreData> = runBlocking {
        Optional.ofNullable(MlbStatsApi.boxscoreDataEither(gamePk = gamePk).getOrNull())
    }

    @JvmStatic
    fun linescore(gamePk: Long): String = runBlocking {
        MlbStatsApi.linescore(gamePk = gamePk)
    }

    @JvmStatic
    fun lookupPlayer(name: String): List<MlbPlayerLookup> = runBlocking {
        MlbStatsApi.lookupPlayer(lookupValue = name)
    }

    @JvmStatic
    fun lookupFirstPlayer(name: String): Optional<MlbPlayerLookup> = runBlocking {
        Optional.ofNullable(MlbStatsApi.lookupPlayer(lookupValue = name).firstOrNull())
    }

    @JvmStatic
    fun lookupTeam(name: String): List<MlbTeamLookup> = runBlocking {
        MlbStatsApi.lookupTeam(lookupValue = name)
    }

    @JvmStatic
    fun lookupFirstTeam(name: String): Optional<MlbTeamLookup> = runBlocking {
        Optional.ofNullable(MlbStatsApi.lookupTeam(lookupValue = name).firstOrNull())
    }

    @JvmStatic
    fun roster(teamId: Int): List<MlbRosterMember> = runBlocking {
        MlbStatsApi.roster(teamId = teamId)
    }

    @JvmStatic
    fun lastGame(teamId: Int): Optional<Long> = runBlocking {
        Optional.ofNullable(MlbStatsApi.lastGame(teamId))
    }

    @JvmStatic
    fun nextGame(teamId: Int): Optional<Long> = runBlocking {
        Optional.ofNullable(MlbStatsApi.nextGame(teamId))
    }

    // =========================================================================
    // 2. Asynchronous CompletableFuture Methods
    // =========================================================================

    @JvmStatic
    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
    fun scheduleAsync(date: String): CompletableFuture<List<MlbScheduleGame>> = GlobalScope.future {
        MlbStatsApi.schedule(date = date)
    }

    @JvmStatic
    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
    fun standingsAsync(season: Int): CompletableFuture<List<MlbDivisionStandings>> = GlobalScope.future {
        MlbStatsApi.standings(season = season)
    }
}
