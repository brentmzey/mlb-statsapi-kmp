package com.sabermetrics.statsapi.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

// ==============================================================================
// 1. Schedule & Game Models
// ==============================================================================

@Serializable
data class MlbScheduleGame(
    val gameId: Long,
    val gameDatetime: String,
    val gameDate: String,
    val gameType: String,
    val status: String,
    val awayName: String,
    val homeName: String,
    val awayId: Int,
    val homeId: Int,
    val doubleHeader: String = "N",
    val gameNumber: Int = 1,
    val homeProbablePitcher: String = "",
    val awayProbablePitcher: String = "",
    val awayScore: Int = 0,
    val homeScore: Int = 0,
    val currentInning: String = "",
    val inningState: String = "",
    val venueId: Int? = null,
    val venueName: String = "",
    val winningTeam: String? = null,
    val losingTeam: String? = null,
    val winningPitcher: String? = null,
    val losingPitcher: String? = null,
    val savePitcher: String? = null,
    val seriesStatus: String? = null,
    val nationalBroadcasts: List<String> = emptyList(),
    val summary: String = ""
)

// ==============================================================================
// 2. Standings Models
// ==============================================================================

@Serializable
data class MlbTeamStandingRecord(
    val teamId: Int,
    val teamName: String,
    val season: String,
    val wins: Int,
    val losses: Int,
    val runDifferential: Int,
    val runsScored: Double,
    val runsAllowed: Double,
    val winPct: Double,
    val gamesBack: String = "-",
    val wildcardGamesBack: String = "-",
    val streakCode: String = "",
    val divisionRank: String = "1",
    val leagueRank: String = "1",
    val last10Wins: Int = 5,
    val last10Losses: Int = 5
)

@Serializable
data class MlbDivisionStandings(
    val divisionId: Int,
    val divisionName: String,
    val leagueId: Int,
    val leagueName: String,
    val standingsType: String,
    val teamRecords: List<MlbTeamStandingRecord>
)

// ==============================================================================
// 3. Boxscore & Linescore Models
// ==============================================================================

@Serializable
data class MlbBatterBoxRow(
    val nameField: String,
    val ab: String,
    val r: String,
    val h: String,
    val rbi: String,
    val bb: String,
    val k: String,
    val lob: String,
    val avg: String,
    val ops: String,
    val personId: Long = 0L,
    val position: String = ""
)

@Serializable
data class MlbPitcherBoxRow(
    val nameField: String,
    val ip: String,
    val h: String,
    val r: String,
    val er: String,
    val bb: String,
    val k: String,
    val hr: String,
    val era: String,
    val personId: Long = 0L
)

@Serializable
data class MlbBoxscoreData(
    val gameId: Long,
    val awayTeamName: String,
    val homeTeamName: String,
    val awayBatters: List<MlbBatterBoxRow>,
    val homeBatters: List<MlbBatterBoxRow>,
    val awayPitchers: List<MlbPitcherBoxRow>,
    val homePitchers: List<MlbPitcherBoxRow>,
    val battingInfo: Map<String, List<String>> = emptyMap(),
    val fieldingInfo: Map<String, List<String>> = emptyMap(),
    val gameNotes: List<String> = emptyList()
)

@Serializable
data class MlbInningScore(
    val num: Int,
    val ordinalNum: String,
    val awayRuns: Int? = null,
    val homeRuns: Int? = null,
    val awayHits: Int? = null,
    val homeHits: Int? = null,
    val awayErrors: Int? = null,
    val homeErrors: Int? = null
)

@Serializable
data class MlbLinescoreData(
    val gamePk: Long,
    val currentInning: Int = 9,
    val currentInningOrdinal: String = "9th",
    val inningState: String = "Final",
    val awayTeamName: String,
    val homeTeamName: String,
    val awayRuns: Int,
    val homeRuns: Int,
    val awayHits: Int,
    val homeHits: Int,
    val awayErrors: Int,
    val homeErrors: Int,
    val innings: List<MlbInningScore>
)

// ==============================================================================
// 4. Player, Roster & Leader Models
// ==============================================================================

@Serializable
data class MlbPlayerLookup(
    val id: Long,
    val fullName: String,
    val firstName: String = "",
    val lastName: String = "",
    val primaryNumber: String = "",
    val currentTeamId: Int = 0,
    val currentTeamName: String = "",
    val primaryPosition: String = "",
    val active: Boolean = true
)

@Serializable
data class MlbTeamLookup(
    val id: Int,
    val name: String,
    val teamCode: String = "",
    val abbreviation: String = "",
    val leagueId: Int = 0,
    val leagueName: String = "",
    val divisionId: Int = 0,
    val divisionName: String = "",
    val venueName: String = "",
    val active: Boolean = true
)

@Serializable
data class MlbRosterMember(
    val personId: Long,
    val fullName: String,
    val jerseyNumber: String = "",
    val positionCode: String = "",
    val positionName: String = "",
    val positionType: String = "",
    val status: String = "Active"
)

@Serializable
data class MlbLeaderEntry(
    val rank: Int,
    val personId: Long,
    val personName: String,
    val teamName: String,
    val value: String,
    val season: String
)

@Serializable
data class MlbLeaderCategory(
    val category: String,
    val statGroup: String,
    val leaders: List<MlbLeaderEntry>
)

@Serializable
data class MlbGamePaceData(
    val season: Int,
    val sportId: Int,
    val totalGames: Int,
    val timePer9Innings: String,
    val timePerGame: String,
    val pitchesPerGame: Double,
    val plateAppearancesPerGame: Double
)
