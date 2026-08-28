package com.sabermetrics.statsapi.java

import com.sabermetrics.statsapi.MlbStatsApi
import com.sabermetrics.statsapi.models.*
import kotlinx.coroutines.future.future
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.GlobalScope

/**
 * Java 17+ Interoperability Adapter for MLB-StatsAPI KMP.
 * Provides synchronous blocking and `CompletableFuture` asynchronous methods.
 */
object MlbStatsApiJava {

    // =========================================================================
    // Synchronous Blocking Methods for Java
    // =========================================================================

    @JvmStatic
    fun schedule(date: String): List<MlbScheduleGame> = runBlocking {
        MlbStatsApi.schedule(date = date)
    }

    @JvmStatic
    fun standings(season: Int): List<MlbDivisionStandings> = runBlocking {
        MlbStatsApi.standings(season = season)
    }

    @JvmStatic
    fun boxscore(gamePk: Long): String = runBlocking {
        MlbStatsApi.boxscore(gamePk = gamePk)
    }

    @JvmStatic
    fun boxscoreData(gamePk: Long): MlbBoxscoreData = runBlocking {
        MlbStatsApi.boxscoreData(gamePk = gamePk)
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
    fun lookupTeam(name: String): List<MlbTeamLookup> = runBlocking {
        MlbStatsApi.lookupTeam(lookupValue = name)
    }

    @JvmStatic
    fun roster(teamId: Int): List<MlbRosterMember> = runBlocking {
        MlbStatsApi.roster(teamId = teamId)
    }

    // =========================================================================
    // Asynchronous CompletableFuture Methods for Java
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
