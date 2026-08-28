package com.sabermetrics.statsapi.cli

import arrow.core.fold
import com.sabermetrics.statsapi.MlbStatsApi
import kotlinx.coroutines.runBlocking

/**
 * Runnable CLI Application for MLB-StatsAPI KMP.
 */
object MlbStatsCli {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        println("==============================================================================")
        println(" ⚾ MLB-StatsAPI Kotlin Multiplatform CLI Application")
        println("==============================================================================")

        // 1. Fetch Today's Live Schedule
        println("\n📅 1. Fetching Live MLB Schedule...")
        val scheduleResult = MlbStatsApi.scheduleEither()
        scheduleResult.fold(
            ifLeft = { error ->
                println("⚠️ Could not load live schedule: ${error.message}")
            },
            ifRight = { games ->
                if (games.isEmpty()) {
                    println("ℹ️ No games scheduled for the selected date.")
                } else {
                    println("✅ Found ${games.size} games:")
                    games.take(5).forEach { g ->
                        println("   • ${g.summary}")
                    }
                }
            }
        )

        // 2. Fetch Division Standings
        println("\n🏆 2. Fetching MLB Division Standings (2026 Season)...")
        val standingsResult = MlbStatsApi.standingsEither(season = 2026)
        standingsResult.fold(
            ifLeft = { error ->
                println("⚠️ Could not load standings: ${error.message}")
            },
            ifRight = { divisions ->
                println("✅ Loaded ${divisions.size} MLB Divisions:")
                divisions.forEach { div ->
                    println("\n   📌 ${div.divisionName} (${div.leagueName})")
                    div.teamRecords.take(3).forEachIndexed { idx, t ->
                        println("      ${idx + 1}. ${t.teamName.padEnd(26)} ${t.wins}-${t.losses} (Diff: %+d)".format(t.runDifferential))
                    }
                }
            }
        )

        // 3. Player Search
        println("\n🔍 3. Searching for Player: 'Shohei Ohtani'...")
        val playerResult = MlbStatsApi.lookupPlayerEither("Shohei Ohtani")
        playerResult.fold(
            ifLeft = { error ->
                println("⚠️ Player search failed: ${error.message}")
            },
            ifRight = { players ->
                players.forEach { p ->
                    println("   • ${p.fullName} (#${p.primaryNumber}) | Position: ${p.primaryPosition} | Person ID: ${p.id}")
                }
            }
        )

        println("\n==============================================================================")
        println(" ✅ MLB-StatsAPI Multiplatform CLI Completed Successfully!")
        println("==============================================================================")
    }
}
