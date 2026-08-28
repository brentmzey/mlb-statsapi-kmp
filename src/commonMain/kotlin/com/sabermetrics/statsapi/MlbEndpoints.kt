package com.sabermetrics.statsapi

/**
 * MLB Stats API Endpoint Constants & Query Path Resolution.
 * Complete 1:1 parity with canonical endpoints configured in Python `toddrob99/MLB-StatsAPI`.
 */
object MlbEndpoints {
    const val BASE_URL: String = "https://statsapi.mlb.com/api/"

    val ENDPOINTS: Map<String, String> = mapOf(
        "attendance" to "v1/attendance",
        "awards" to "v1/awards{awardId}{recipients}",
        "conferences" to "v1/conferences",
        "divisions" to "v1/divisions",
        "draft" to "v1/draft{prospects}{year}{latest}",
        "game" to "v1.1/game/{gamePk}/feed/live",
        "game_boxscore" to "v1/game/{gamePk}/boxscore",
        "game_changes" to "v1/game/changes",
        "game_color" to "v1/game/{gamePk}/feed/color",
        "game_color_diff" to "v1/game/{gamePk}/feed/color/diffPatch",
        "game_color_timestamps" to "v1/game/{gamePk}/feed/color/timestamps",
        "game_content" to "v1/game/{gamePk}/content",
        "game_contextMetrics" to "v1/game/{gamePk}/contextMetrics",
        "game_diff" to "v1.1/game/{gamePk}/feed/live/diffPatch",
        "game_linescore" to "v1/game/{gamePk}/linescore",
        "game_playByPlay" to "v1/game/{gamePk}/playByPlay",
        "game_timestamps" to "v1.1/game/{gamePk}/feed/live/timestamps",
        "game_uniforms" to "v1/game/{gamePk}/uniforms",
        "game_winProbability" to "v1/game/{gamePk}/winProbability",
        "gamePace" to "v1/gamePace",
        "highLow" to "v1/highLow/{type}",
        "homeRunDerby" to "v1/homeRunDerby/{gamePk}",
        "jobs" to "v1/jobs/{jobType}",
        "jobs_datacasters" to "v1/jobs/datacasters",
        "jobs_officialScorers" to "v1/jobs/officialScorers",
        "jobs_umpires" to "v1/jobs/umpires",
        "jobs_umpire_games" to "v1/jobs/umpires/games/{umpireId}",
        "league" to "v1/league",
        "league_allStarBallot" to "v1/league/{leagueId}/allStarBallot",
        "league_allStarWriteIns" to "v1/league/{leagueId}/allStarWriteIns",
        "league_allStarFinalVote" to "v1/league/{leagueId}/allStarFinalVote",
        "league_leaders" to "v1/stats/leaders",
        "league_leader_types" to "v1/leagueLeaderTypes",
        "meta" to "v1/{type}",
        "people" to "v1/people",
        "people_search" to "v1/people/search",
        "person" to "v1/people/{personId}",
        "people_changes" to "v1/people/changes",
        "people_freeAgents" to "v1/people/freeAgents",
        "person_stats" to "v1/people/{personId}/stats",
        "schedule" to "v1/schedule",
        "schedule_tied" to "v1/schedule/tied",
        "schedule_postseason" to "v1/schedule/postseason",
        "schedule_postseason_series" to "v1/schedule/postseason/series",
        "schedule_postseason_tuneIn" to "v1/schedule/postseason/tuneIn",
        "season" to "v1/seasons/{seasonId}",
        "seasons" to "v1/seasons",
        "sports" to "v1/sports",
        "sports_players" to "v1/sports/{sportId}/players",
        "standings" to "v1/standings",
        "standings_types" to "v1/standingsTypes",
        "stats" to "v1/stats",
        "stats_leaders" to "v1/stats/leaders",
        "stats_streaks" to "v1/stats/streaks",
        "team" to "v1/teams/{teamId}",
        "teams" to "v1/teams",
        "teams_history" to "v1/teams/history",
        "teams_stats" to "v1/teams/stats",
        "teams_affiliates" to "v1/teams/affiliates",
        "team_affiliates" to "v1/teams/{teamId}/affiliates",
        "team_alumni" to "v1/teams/{teamId}/alumni",
        "team_coaches" to "v1/teams/{teamId}/coaches",
        "team_history" to "v1/teams/{teamId}/history",
        "team_leaders" to "v1/teams/{teamId}/leaders",
        "team_personnel" to "v1/teams/{teamId}/personnel",
        "team_roster" to "v1/teams/{teamId}/roster",
        "team_stats" to "v1/teams/{teamId}/stats",
        "team_uniforms" to "v1/teams/{teamId}/uniforms",
        "transactions" to "v1/transactions",
        "venue" to "v1/venues/{venueId}",
        "venues" to "v1/venues",
        "awards_types" to "v1/awards",
        "stat_types" to "v1/statTypes",
        "stat_groups" to "v1/statGroups",
        "event_types" to "v1/eventTypes",
        "roster_types" to "v1/rosterTypes",
        "game_types" to "v1/gameTypes",
        "game_status_codes" to "v1/gameStatus"
    )

    /**
     * Resolves an endpoint path template with given path arguments.
     * e.g. "game_boxscore" with gamePk=745678 -> "https://statsapi.mlb.com/api/v1/game/745678/boxscore"
     */
    fun resolveUrl(endpointKey: String, pathParams: Map<String, Any> = emptyMap()): String {
        val pathTemplate = ENDPOINTS[endpointKey] ?: endpointKey
        var resolvedPath = pathTemplate
        for ((key, value) in pathParams) {
            resolvedPath = resolvedPath.replace("{$key}", value.toString())
        }
        return if (resolvedPath.startsWith("http")) {
            resolvedPath
        } else {
            BASE_URL + resolvedPath
        }
    }
}
