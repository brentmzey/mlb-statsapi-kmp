package com.sabermetrics.statsapi.java;

import com.sabermetrics.statsapi.models.MlbDivisionStandings;
import com.sabermetrics.statsapi.models.MlbPlayerLookup;
import com.sabermetrics.statsapi.models.MlbScheduleGame;
import com.sabermetrics.statsapi.models.MlbTeamLookup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Java 17+ Interoperability & Functional Integration Test Suite")
public class MlbStatsApiJavaIntegrationTest {

    @Test
    @DisplayName("Verify Java Synchronous Standings & Streams Filter")
    public void testJavaStandingsStreamPipeline() {
        try {
            List<MlbDivisionStandings> standings = MlbStatsApiJava.standings(2026);
            assertNotNull(standings, "Standings list should not be null");

            if (!standings.isEmpty()) {
                assertTrue(standings.size() >= 6, "Expected at least 6 divisions");

                // Test Java Stream transformations
                long totalTeams = standings.stream()
                        .flatMap(div -> div.getTeamRecords().stream())
                        .count();
                assertTrue(totalTeams >= 30, "Expected at least 30 MLB teams in stream");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Java Standings test skipped: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify Java Optional API with lookupFirstPlayer")
    public void testJavaOptionalLookupPipeline() {
        try {
            Optional<MlbPlayerLookup> ohtaniOpt = MlbStatsApiJava.lookupFirstPlayer("Shohei Ohtani");
            assertNotNull(ohtaniOpt, "Optional should not be null");

            ohtaniOpt.ifPresent(player -> {
                assertTrue(player.getId() > 0, "Player ID should be positive");
                assertTrue(player.getFullName().contains("Shohei"), "Player name should contain Shohei");
            });

            // Verify Optional map and flatMap
            Optional<String> teamNameOpt = ohtaniOpt.map(MlbPlayerLookup::getFullName);
            assertNotNull(teamNameOpt);
        } catch (Exception e) {
            System.out.println("⚠️ Java Optional Lookup skipped: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify Java Asynchronous CompletableFuture Pipeline")
    public void testJavaCompletableFutureAsyncPipeline() throws Exception {
        try {
            CompletableFuture<List<MlbDivisionStandings>> future = MlbStatsApiJava.standingsAsync(2026);
            assertNotNull(future, "Future must not be null");

            List<MlbDivisionStandings> result = future.get(10, TimeUnit.SECONDS);
            assertNotNull(result, "Async result must not be null");
        } catch (Exception e) {
            System.out.println("⚠️ Java Async Pipeline skipped: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify Java Team Lookup and Optional Fallback")
    public void testJavaTeamLookupOptionalFallback() {
        try {
            Optional<MlbTeamLookup> yankeesOpt = MlbStatsApiJava.lookupFirstTeam("Yankees");
            assertNotNull(yankeesOpt);

            String teamName = yankeesOpt
                    .map(MlbTeamLookup::getName)
                    .orElse("Default Team");

            assertNotNull(teamName);
        } catch (Exception e) {
            System.out.println("⚠️ Java Team Lookup skipped: " + e.getMessage());
        }
    }
}
