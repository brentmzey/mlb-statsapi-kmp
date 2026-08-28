package com.sabermetrics.statsapi

import arrow.core.*
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.sabermetrics.statsapi.error.MlbStatsError
import com.sabermetrics.statsapi.formatters.MlbTableFormatter
import com.sabermetrics.statsapi.models.*
import kotlinx.serialization.json.*
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * MLB-StatsAPI Kotlin Multiplatform SDK.
 * Functional, strongly-typed, and cross-platform MLB Stats API client powered by Arrow KT.
 */
object MlbStatsApi {

    val client: MlbHttpClient = MlbHttpClient()

    // =========================================================================
    // 1. Generic Universal GET (Functional & Standard)
    // =========================================================================

    @JvmStatic
    @JvmOverloads
    suspend fun getEither(endpoint: String, params: Map<String, Any> = emptyMap()): Either<MlbStatsError, JsonObject> {
        val url = MlbEndpoints.resolveUrl(endpoint, params)
        return client.getJsonObjectEither(url, params)
    }

    @JvmStatic
    @JvmOverloads
    suspend fun get(endpoint: String, params: Map<String, Any> = emptyMap()): JsonObject {
        return getEither(endpoint, params).getOrElse { JsonObject(emptyMap()) }
    }

    // =========================================================================
    // 2. Schedule (Functional & Standard)
    // =========================================================================

    @JvmStatic
    @JvmOverloads
    suspend fun scheduleEither(
        date: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        teamId: Int? = null,
        opponentId: Int? = null,
        sportId: Int = 1,
        gamePk: Long? = null,
        leagueId: Int? = null,
        season: Int? = null,
        includeSeriesStatus: Boolean = true
    ): Either<MlbStatsError, List<MlbScheduleGame>> = either {
        val params = mutableMapOf<String, Any>()
        if (date != null) {
            params["date"] = date
        } else if (startDate != null && endDate != null) {
            params["startDate"] = startDate
            params["endDate"] = endDate
        }
        if (teamId != null) params["teamId"] = teamId
        if (opponentId != null) params["opponentId"] = opponentId
        if (gamePk != null) params["gamePks"] = gamePk
        if (leagueId != null) params["leagueId"] = leagueId
        if (season != null) params["season"] = season

        var hydrate = "decisions,probablePitcher(note),linescore,broadcasts,game(content(media(epg)))"
        if (includeSeriesStatus) hydrate += ",seriesStatus"
        params["sportId"] = sportId
        params["hydrate"] = hydrate

        val url = MlbEndpoints.resolveUrl("schedule")
        val root = client.getJsonObjectEither(url, params).bind()

        val datesArray = root["dates"]?.jsonArray ?: return@either emptyList()

        datesArray.flatMap { dateElem ->
            val dateObj = dateElem.jsonObject
            val dStr = dateObj["date"]?.jsonPrimitive?.content ?: ""
            val games = dateObj["games"]?.jsonArray ?: emptyList()

            games.mapNotNull { gElem ->
                val g = gElem.jsonObject
                val gId = g["gamePk"]?.jsonPrimitive?.longOrNull ?: return@mapNotNull null
                val gDt = g["gameDate"]?.jsonPrimitive?.content ?: ""
                val gType = g["gameType"]?.jsonPrimitive?.content ?: "R"
                val status = g["status"]?.jsonObject?.get("detailedState")?.jsonPrimitive?.content ?: ""

                val teamsObj = g["teams"]?.jsonObject
                val awayObj = teamsObj?.get("away")?.jsonObject
                val homeObj = teamsObj?.get("home")?.jsonObject

                val awayName = awayObj?.get("team")?.jsonObject?.get("name")?.jsonPrimitive?.content ?: "Unknown"
                val homeName = homeObj?.get("team")?.jsonObject?.get("name")?.jsonPrimitive?.content ?: "Unknown"
                val awayId = awayObj?.get("team")?.jsonObject?.get("id")?.jsonPrimitive?.intOrNull ?: 0
                val homeId = homeObj?.get("team")?.jsonObject?.get("id")?.jsonPrimitive?.intOrNull ?: 0

                val awayScore = awayObj?.get("score")?.jsonPrimitive?.intOrNull ?: 0
                val homeScore = homeObj?.get("score")?.jsonPrimitive?.intOrNull ?: 0

                val awayProbable = awayObj?.get("probablePitcher")?.jsonObject?.get("fullName")?.jsonPrimitive?.content ?: ""
                val homeProbable = homeObj?.get("probablePitcher")?.jsonObject?.get("fullName")?.jsonPrimitive?.content ?: ""

                val venueObj = g["venue"]?.jsonObject
                val venueId = venueObj?.get("id")?.jsonPrimitive?.intOrNull
                val venueName = venueObj?.get("name")?.jsonPrimitive?.content ?: ""

                val isAwayWinner = awayObj?.get("isWinner")?.jsonPrimitive?.booleanOrNull ?: false
                val isHomeWinner = homeObj?.get("isWinner")?.jsonPrimitive?.booleanOrNull ?: false
                val winningTeam = if (isAwayWinner) awayName else if (isHomeWinner) homeName else null
                val losingTeam = if (isAwayWinner) homeName else if (isHomeWinner) awayName else null

                val decisions = g["decisions"]?.jsonObject
                val winPitcher = decisions?.get("winner")?.jsonObject?.get("fullName")?.jsonPrimitive?.content
                val losePitcher = decisions?.get("loser")?.jsonObject?.get("fullName")?.jsonPrimitive?.content
                val savePitcher = decisions?.get("save")?.jsonObject?.get("fullName")?.jsonPrimitive?.content

                val seriesStatus = g["seriesStatus"]?.jsonObject?.get("result")?.jsonPrimitive?.content

                val summary = if (status in listOf("Final", "Game Over")) {
                    "$dStr - $awayName ($awayScore) @ $homeName ($homeScore) ($status)"
                } else {
                    "$dStr - $awayName @ $homeName ($status)"
                }

                MlbScheduleGame(
                    gameId = gId,
                    gameDatetime = gDt,
                    gameDate = dStr,
                    gameType = gType,
                    status = status,
                    awayName = awayName,
                    homeName = homeName,
                    awayId = awayId,
                    homeId = homeId,
                    homeProbablePitcher = homeProbable,
                    awayProbablePitcher = awayProbable,
                    awayScore = awayScore,
                    homeScore = homeScore,
                    venueId = venueId,
                    venueName = venueName,
                    winningTeam = winningTeam,
                    losingTeam = losingTeam,
                    winningPitcher = winPitcher,
                    losingPitcher = losePitcher,
                    savePitcher = savePitcher,
                    seriesStatus = seriesStatus,
                    summary = summary
                )
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    suspend fun schedule(
        date: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        teamId: Int? = null,
        opponentId: Int? = null,
        sportId: Int = 1,
        gamePk: Long? = null,
        leagueId: Int? = null,
        season: Int? = null,
        includeSeriesStatus: Boolean = true
    ): List<MlbScheduleGame> {
        return scheduleEither(
            date = date,
            startDate = startDate,
            endDate = endDate,
            teamId = teamId,
            opponentId = opponentId,
            sportId = sportId,
            gamePk = gamePk,
            leagueId = leagueId,
            season = season,
            includeSeriesStatus = includeSeriesStatus
        ).getOrElse { emptyList() }
    }

    // =========================================================================
    // 3. Standings (Functional & Standard)
    // =========================================================================

    @JvmStatic
    @JvmOverloads
    suspend fun standingsEither(
        season: Int? = null,
        leagueId: String = "103,104",
        divisionId: Int? = null
    ): Either<MlbStatsError, List<MlbDivisionStandings>> = either {
        val params = mutableMapOf<String, Any>("leagueId" to leagueId)
        if (season != null) params["season"] = season
        if (divisionId != null) params["divisionId"] = divisionId

        val url = MlbEndpoints.resolveUrl("standings")
        val root = client.getJsonObjectEither(url, params).bind()

        val records = root["records"]?.jsonArray ?: return@either emptyList()

        records.mapNotNull { recElem ->
            val rec = recElem.jsonObject
            val stType = rec["standingsType"]?.jsonPrimitive?.content ?: "regularSeason"
            val leagueObj = rec["league"]?.jsonObject
            val divObj = rec["division"]?.jsonObject

            val lId = leagueObj?.get("id")?.jsonPrimitive?.intOrNull ?: 0
            val lName = leagueObj?.get("name")?.jsonPrimitive?.content ?: ""
            val dId = divObj?.get("id")?.jsonPrimitive?.intOrNull ?: 0
            val dName = divObj?.get("name")?.jsonPrimitive?.content ?: ""

            val trArray = rec["teamRecords"]?.jsonArray ?: return@mapNotNull null
            val teamRecords = trArray.mapNotNull { trElem ->
                val tr = trElem.jsonObject
                val tObj = tr["team"]?.jsonObject
                val teamId = tObj?.get("id")?.jsonPrimitive?.intOrNull ?: return@mapNotNull null
                val teamName = tObj["name"]?.jsonPrimitive?.content ?: "Unknown"
                val seasonStr = tr["season"]?.jsonPrimitive?.content ?: season?.toString() ?: "2026"

                val wins = tr["wins"]?.jsonPrimitive?.intOrNull ?: 0
                val losses = tr["losses"]?.jsonPrimitive?.intOrNull ?: 0
                val rs = tr["runsScored"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                val ra = tr["runsAllowed"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                val diff = tr["runDifferential"]?.jsonPrimitive?.intOrNull ?: (rs - ra).toInt()

                val winPct = if (wins + losses > 0) wins.toDouble() / (wins + losses) else 0.500
                val gb = tr["gamesBack"]?.jsonPrimitive?.content ?: "-"
                val wcGb = tr["wildCardGamesBack"]?.jsonPrimitive?.content ?: "-"
                val streak = tr["streak"]?.jsonObject?.get("streakCode")?.jsonPrimitive?.content ?: ""
                val divRank = tr["divisionRank"]?.jsonPrimitive?.content ?: "1"
                val lgRank = tr["leagueRank"]?.jsonPrimitive?.content ?: "1"

                val splits = tr["records"]?.jsonObject?.get("splitRecords")?.jsonArray ?: emptyList()
                val l10 = splits.firstOrNull { it.jsonObject["type"]?.jsonPrimitive?.content == "lastTen" }?.jsonObject
                val l10W = l10?.get("wins")?.jsonPrimitive?.intOrNull ?: 5
                val l10L = l10?.get("losses")?.jsonPrimitive?.intOrNull ?: 5

                MlbTeamStandingRecord(
                    teamId = teamId,
                    teamName = teamName,
                    season = seasonStr,
                    wins = wins,
                    losses = losses,
                    runDifferential = diff,
                    runsScored = rs,
                    runsAllowed = ra,
                    winPct = winPct,
                    gamesBack = gb,
                    wildcardGamesBack = wcGb,
                    streakCode = streak,
                    divisionRank = divRank,
                    leagueRank = lgRank,
                    last10Wins = l10W,
                    last10Losses = l10L
                )
            }

            MlbDivisionStandings(
                divisionId = dId,
                divisionName = dName,
                leagueId = lId,
                leagueName = lName,
                standingsType = stType,
                teamRecords = teamRecords
            )
        }
    }

    @JvmStatic
    @JvmOverloads
    suspend fun standings(
        season: Int? = null,
        leagueId: String = "103,104",
        divisionId: Int? = null
    ): List<MlbDivisionStandings> {
        return standingsEither(season, leagueId, divisionId).getOrElse { emptyList() }
    }

    // =========================================================================
    // 4. Boxscore & Linescore (Functional & Standard)
    // =========================================================================

    @JvmStatic
    @JvmOverloads
    suspend fun boxscoreDataEither(gamePk: Long, timecode: String? = null): Either<MlbStatsError, MlbBoxscoreData> = either {
        val params = mutableMapOf<String, Any>("gamePk" to gamePk)
        if (timecode != null) params["timecode"] = timecode

        val url = MlbEndpoints.resolveUrl("game_boxscore", mapOf("gamePk" to gamePk))
        val root = client.getJsonObjectEither(url, params).bind()

        val teams = root["teams"]?.jsonObject
        val away = teams?.get("away")?.jsonObject
        val home = teams?.get("home")?.jsonObject

        val awayTeamName = away?.get("team")?.jsonObject?.get("name")?.jsonPrimitive?.content ?: "Away"
        val homeTeamName = home?.get("team")?.jsonObject?.get("name")?.jsonPrimitive?.content ?: "Home"

        val parseBatters = { tObj: JsonObject? ->
            val bIds = tObj?.get("batters")?.jsonArray ?: emptyList()
            val players = tObj?.get("players")?.jsonObject
            bIds.mapNotNull { idElem ->
                val pId = idElem.jsonPrimitive.content
                val p = players?.get("ID$pId")?.jsonObject ?: return@mapNotNull null
                val stats = p["stats"]?.jsonObject?.get("batting")?.jsonObject ?: return@mapNotNull null
                val boxName = p["person"]?.jsonObject?.get("fullName")?.jsonPrimitive?.content ?: ""
                val pos = p["position"]?.jsonObject?.get("abbreviation")?.jsonPrimitive?.content ?: ""

                MlbBatterBoxRow(
                    nameField = "$boxName $pos".trim(),
                    ab = stats["atBats"]?.jsonPrimitive?.content ?: "0",
                    r = stats["runs"]?.jsonPrimitive?.content ?: "0",
                    h = stats["hits"]?.jsonPrimitive?.content ?: "0",
                    rbi = stats["rbi"]?.jsonPrimitive?.content ?: "0",
                    bb = stats["baseOnBalls"]?.jsonPrimitive?.content ?: "0",
                    k = stats["strikeOuts"]?.jsonPrimitive?.content ?: "0",
                    lob = stats["leftOnBase"]?.jsonPrimitive?.content ?: "0",
                    avg = stats["avg"]?.jsonPrimitive?.content ?: ".000",
                    ops = stats["ops"]?.jsonPrimitive?.content ?: ".000",
                    personId = pId.toLongOrNull() ?: 0L,
                    position = pos
                )
            }
        }

        val parsePitchers = { tObj: JsonObject? ->
            val pIds = tObj?.get("pitchers")?.jsonArray ?: emptyList()
            val players = tObj?.get("players")?.jsonObject
            pIds.mapNotNull { idElem ->
                val pId = idElem.jsonPrimitive.content
                val p = players?.get("ID$pId")?.jsonObject ?: return@mapNotNull null
                val stats = p["stats"]?.jsonObject?.get("pitching")?.jsonObject ?: return@mapNotNull null
                val boxName = p["person"]?.jsonObject?.get("fullName")?.jsonPrimitive?.content ?: ""

                MlbPitcherBoxRow(
                    nameField = boxName,
                    ip = stats["inningsPitched"]?.jsonPrimitive?.content ?: "0.0",
                    h = stats["hits"]?.jsonPrimitive?.content ?: "0",
                    r = stats["runs"]?.jsonPrimitive?.content ?: "0",
                    er = stats["earnedRuns"]?.jsonPrimitive?.content ?: "0",
                    bb = stats["baseOnBalls"]?.jsonPrimitive?.content ?: "0",
                    k = stats["strikeOuts"]?.jsonPrimitive?.content ?: "0",
                    hr = stats["homeRuns"]?.jsonPrimitive?.content ?: "0",
                    era = stats["era"]?.jsonPrimitive?.content ?: "0.00",
                    personId = pId.toLongOrNull() ?: 0L
                )
            }
        }

        MlbBoxscoreData(
            gameId = gamePk,
            awayTeamName = awayTeamName,
            homeTeamName = homeTeamName,
            awayBatters = parseBatters(away),
            homeBatters = parseBatters(home),
            awayPitchers = parsePitchers(away),
            homePitchers = parsePitchers(home)
        )
    }

    @JvmStatic
    @JvmOverloads
    suspend fun boxscoreData(gamePk: Long, timecode: String? = null): MlbBoxscoreData {
        return boxscoreDataEither(gamePk, timecode).fold(
            ifLeft = { throw RuntimeException(it.message) },
            ifRight = { it }
        )
    }

    @JvmStatic
    @JvmOverloads
    suspend fun boxscore(gamePk: Long, timecode: String? = null): String {
        return boxscoreDataEither(gamePk, timecode).fold(
            ifLeft = { "Error loading boxscore: ${it.message}" },
            ifRight = { MlbTableFormatter.formatBoxscore(it) }
        )
    }

    @JvmStatic
    @JvmOverloads
    suspend fun linescoreEither(gamePk: Long, timecode: String? = null): Either<MlbStatsError, String> = either {
        val url = MlbEndpoints.resolveUrl("game_linescore", mapOf("gamePk" to gamePk))
        val root = client.getJsonObjectEither(url).bind()

        val teams = root["teams"]?.jsonObject
        val away = teams?.get("away")?.jsonObject
        val home = teams?.get("home")?.jsonObject

        val inningsList = mutableListOf<MlbInningScore>()
        val innArray = root["innings"]?.jsonArray ?: emptyList()
        for (innElem in innArray) {
            val inn = innElem.jsonObject
            val num = inn["num"]?.jsonPrimitive?.intOrNull ?: 1
            val ord = inn["ordinalNum"]?.jsonPrimitive?.content ?: "${num}th"
            val aRuns = inn["away"]?.jsonObject?.get("runs")?.jsonPrimitive?.intOrNull
            val hRuns = inn["home"]?.jsonObject?.get("runs")?.jsonPrimitive?.intOrNull
            val aHits = inn["away"]?.jsonObject?.get("hits")?.jsonPrimitive?.intOrNull
            val hHits = inn["home"]?.jsonObject?.get("hits")?.jsonPrimitive?.intOrNull
            val aErr = inn["away"]?.jsonObject?.get("errors")?.jsonPrimitive?.intOrNull
            val hErr = inn["home"]?.jsonObject?.get("errors")?.jsonPrimitive?.intOrNull

            inningsList.add(
                MlbInningScore(
                    num = num,
                    ordinalNum = ord,
                    awayRuns = aRuns,
                    homeRuns = hRuns,
                    awayHits = aHits,
                    homeHits = hHits,
                    awayErrors = aErr,
                    homeErrors = hErr
                )
            )
        }

        val linescoreData = MlbLinescoreData(
            gamePk = gamePk,
            awayTeamName = away?.get("team")?.jsonObject?.get("name")?.jsonPrimitive?.content ?: "Away",
            homeTeamName = home?.get("team")?.jsonObject?.get("name")?.jsonPrimitive?.content ?: "Home",
            awayRuns = away?.get("runs")?.jsonPrimitive?.intOrNull ?: 0,
            homeRuns = home?.get("runs")?.jsonPrimitive?.intOrNull ?: 0,
            awayHits = away?.get("hits")?.jsonPrimitive?.intOrNull ?: 0,
            homeHits = home?.get("hits")?.jsonPrimitive?.intOrNull ?: 0,
            awayErrors = away?.get("errors")?.jsonPrimitive?.intOrNull ?: 0,
            homeErrors = home?.get("errors")?.jsonPrimitive?.intOrNull ?: 0,
            innings = inningsList
        )
        MlbTableFormatter.formatLinescore(linescoreData)
    }

    @JvmStatic
    @JvmOverloads
    suspend fun linescore(gamePk: Long, timecode: String? = null): String {
        return linescoreEither(gamePk, timecode).getOrElse { "Error loading linescore: ${it.message}" }
    }

    // =========================================================================
    // 5. Lookups & Player Search (Functional & Standard)
    // =========================================================================

    @JvmStatic
    @JvmOverloads
    suspend fun lookupPlayerEither(lookupValue: String, sportId: Int = 1, season: Int? = null): Either<MlbStatsError, List<MlbPlayerLookup>> = either {
        ensure(lookupValue.isNotBlank()) { MlbStatsError.InvalidParameterError("lookupValue", "Cannot be blank") }

        val params = mutableMapOf<String, Any>("names" to lookupValue, "sportId" to sportId)
        if (season != null) params["season"] = season

        val url = MlbEndpoints.resolveUrl("people")
        val root = client.getJsonObjectEither(url, params).bind()

        val people = root["people"]?.jsonArray ?: return@either emptyList()
        people.map { pElem ->
            val p = pElem.jsonObject
            MlbPlayerLookup(
                id = p["id"]?.jsonPrimitive?.longOrNull ?: 0L,
                fullName = p["fullName"]?.jsonPrimitive?.content ?: "",
                firstName = p["firstName"]?.jsonPrimitive?.content ?: "",
                lastName = p["lastName"]?.jsonPrimitive?.content ?: "",
                primaryNumber = p["primaryNumber"]?.jsonPrimitive?.content ?: "",
                currentTeamId = p["currentTeam"]?.jsonObject?.get("id")?.jsonPrimitive?.intOrNull ?: 0,
                primaryPosition = p["primaryPosition"]?.jsonObject?.get("abbreviation")?.jsonPrimitive?.content ?: "",
                active = p["active"]?.jsonPrimitive?.booleanOrNull ?: true
            )
        }
    }

    @JvmStatic
    @JvmOverloads
    suspend fun lookupPlayer(lookupValue: String, sportId: Int = 1, season: Int? = null): List<MlbPlayerLookup> {
        return lookupPlayerEither(lookupValue, sportId, season).getOrElse { emptyList() }
    }

    @JvmStatic
    @JvmOverloads
    suspend fun lookupTeamEither(lookupValue: String, activeStatus: String = "Y", season: Int? = null): Either<MlbStatsError, List<MlbTeamLookup>> = either {
        ensure(lookupValue.isNotBlank()) { MlbStatsError.InvalidParameterError("lookupValue", "Cannot be blank") }

        val params = mutableMapOf<String, Any>("sportId" to 1, "activeStatus" to activeStatus)
        if (season != null) params["season"] = season

        val url = MlbEndpoints.resolveUrl("teams")
        val root = client.getJsonObjectEither(url, params).bind()

        val teams = root["teams"]?.jsonArray ?: return@either emptyList()
        val query = lookupValue.lowercase()
        teams.mapNotNull { tElem ->
            val t = tElem.jsonObject
            val name = t["name"]?.jsonPrimitive?.content ?: ""
            val code = t["teamCode"]?.jsonPrimitive?.content ?: ""
            val abbrev = t["abbreviation"]?.jsonPrimitive?.content ?: ""

            if (name.lowercase().contains(query) || code.lowercase().contains(query) || abbrev.lowercase().contains(query)) {
                MlbTeamLookup(
                    id = t["id"]?.jsonPrimitive?.intOrNull ?: 0,
                    name = name,
                    teamCode = code,
                    abbreviation = abbrev,
                    leagueId = t["league"]?.jsonObject?.get("id")?.jsonPrimitive?.intOrNull ?: 0,
                    leagueName = t["league"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: "",
                    divisionId = t["division"]?.jsonObject?.get("id")?.jsonPrimitive?.intOrNull ?: 0,
                    divisionName = t["division"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: "",
                    venueName = t["venue"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: "",
                    active = t["active"]?.jsonPrimitive?.booleanOrNull ?: true
                )
            } else null
        }
    }

    @JvmStatic
    @JvmOverloads
    suspend fun lookupTeam(lookupValue: String, activeStatus: String = "Y", season: Int? = null): List<MlbTeamLookup> {
        return lookupTeamEither(lookupValue, activeStatus, season).getOrElse { emptyList() }
    }

    // =========================================================================
    // 6. Roster (Functional & Standard)
    // =========================================================================

    @JvmStatic
    @JvmOverloads
    suspend fun rosterEither(teamId: Int, rosterType: String = "active", season: Int? = null): Either<MlbStatsError, List<MlbRosterMember>> = either {
        val params = mutableMapOf<String, Any>("rosterType" to rosterType)
        if (season != null) params["season"] = season

        val url = MlbEndpoints.resolveUrl("team_roster", mapOf("teamId" to teamId))
        val root = client.getJsonObjectEither(url, params).bind()

        val roster = root["roster"]?.jsonArray ?: return@either emptyList()
        roster.map { rElem ->
            val r = rElem.jsonObject
            val person = r["person"]?.jsonObject
            val pos = r["position"]?.jsonObject
            MlbRosterMember(
                personId = person?.get("id")?.jsonPrimitive?.longOrNull ?: 0L,
                fullName = person?.get("fullName")?.jsonPrimitive?.content ?: "",
                jerseyNumber = r["jerseyNumber"]?.jsonPrimitive?.content ?: "",
                positionCode = pos?.get("code")?.jsonPrimitive?.content ?: "",
                positionName = pos?.get("name")?.jsonPrimitive?.content ?: "",
                positionType = pos?.get("type")?.jsonPrimitive?.content ?: "",
                status = r["status"]?.jsonObject?.get("description")?.jsonPrimitive?.content ?: "Active"
            )
        }
    }

    @JvmStatic
    @JvmOverloads
    suspend fun roster(teamId: Int, rosterType: String = "active", season: Int? = null): List<MlbRosterMember> {
        return rosterEither(teamId, rosterType, season).getOrElse { emptyList() }
    }

    // =========================================================================
    // 7. Last & Next Game
    // =========================================================================

    @JvmStatic
    suspend fun lastGameEither(teamId: Int): Either<MlbStatsError, Long> = either {
        val params = mapOf("teamId" to teamId, "sportId" to 1)
        val url = MlbEndpoints.resolveUrl("schedule")
        val root = client.getJsonObjectEither(url, params).bind()
        val dates = ensureNotNull(root["dates"]?.jsonArray) { MlbStatsError.EntityNotFoundError("Dates", "teamId=$teamId") }
        val lastDate = ensureNotNull(dates.lastOrNull()?.jsonObject) { MlbStatsError.EntityNotFoundError("LastDate", "teamId=$teamId") }
        val games = ensureNotNull(lastDate["games"]?.jsonArray) { MlbStatsError.EntityNotFoundError("Games", "teamId=$teamId") }
        val gamePk = ensureNotNull(games.firstOrNull()?.jsonObject?.get("gamePk")?.jsonPrimitive?.longOrNull) {
            MlbStatsError.EntityNotFoundError("GamePk", "teamId=$teamId")
        }
        gamePk
    }

    @JvmStatic
    suspend fun lastGame(teamId: Int): Long? = lastGameEither(teamId).getOrNull()

    @JvmStatic
    suspend fun nextGameEither(teamId: Int): Either<MlbStatsError, Long> = either {
        val params = mapOf("teamId" to teamId, "sportId" to 1)
        val url = MlbEndpoints.resolveUrl("schedule")
        val root = client.getJsonObjectEither(url, params).bind()
        val dates = ensureNotNull(root["dates"]?.jsonArray) { MlbStatsError.EntityNotFoundError("Dates", "teamId=$teamId") }
        val nextDate = ensureNotNull(dates.firstOrNull()?.jsonObject) { MlbStatsError.EntityNotFoundError("NextDate", "teamId=$teamId") }
        val games = ensureNotNull(nextDate["games"]?.jsonArray) { MlbStatsError.EntityNotFoundError("Games", "teamId=$teamId") }
        val gamePk = ensureNotNull(games.firstOrNull()?.jsonObject?.get("gamePk")?.jsonPrimitive?.longOrNull) {
            MlbStatsError.EntityNotFoundError("GamePk", "teamId=$teamId")
        }
        gamePk
    }

    @JvmStatic
    suspend fun nextGame(teamId: Int): Long? = nextGameEither(teamId).getOrNull()
}
