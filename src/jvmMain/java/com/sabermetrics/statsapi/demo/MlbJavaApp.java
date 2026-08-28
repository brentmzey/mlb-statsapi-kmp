package com.sabermetrics.statsapi.demo;

import com.sabermetrics.statsapi.java.MlbStatsApiJava;
import com.sabermetrics.statsapi.models.MlbDivisionStandings;
import com.sabermetrics.statsapi.models.MlbPlayerLookup;
import com.sabermetrics.statsapi.models.MlbTeamLookup;

import java.util.List;
import java.util.Optional;

/**
 * Pure Java 17+ Application executing MlbStatsApiJava natively.
 */
public class MlbJavaApp {

    public static void main(String[] args) {
        System.out.println("==============================================================================");
        System.out.println(" ☕ MLB-StatsAPI Pure Java 17+ Application Demo");
        System.out.println("==============================================================================");

        // 1. Java Optional Player Lookup
        System.out.println("\n🔍 1. Java Optional Lookup: 'Aaron Judge'");
        Optional<MlbPlayerLookup> judgeOpt = MlbStatsApiJava.lookupFirstPlayer("Aaron Judge");
        judgeOpt.ifPresentOrElse(
                p -> System.out.println("   • Found Player: " + p.getFullName() + " | Position: " + p.getPrimaryPosition() + " | Active: " + p.getActive()),
                () -> System.out.println("   ⚠️ Player not found.")
        );

        // 2. Java Streams Standings Filter
        System.out.println("\n📊 2. Java 17 Streams: Division Leaders & Run Differentials (2026)");
        List<MlbDivisionStandings> standings = MlbStatsApiJava.standings(2026);
        standings.stream()
                .flatMap(div -> div.getTeamRecords().stream())
                .filter(t -> t.getRunDifferential() > 30)
                .map(t -> String.format("   • %-24s (%d-%d, Run Diff: %+d)", t.getTeamName(), t.getWins(), t.getLosses(), t.getRunDifferential()))
                .forEach(System.out::println);

        // 3. Java Team Lookup
        System.out.println("\n⚾ 3. Java Team Lookup: 'Dodgers'");
        Optional<MlbTeamLookup> dodgersOpt = MlbStatsApiJava.lookupFirstTeam("Dodgers");
        dodgersOpt.ifPresent(t -> System.out.println("   • Team: " + t.getName() + " | Division: " + t.getDivisionName() + " | Code: " + t.getTeamCode()));

        System.out.println("\n==============================================================================");
        System.out.println(" ✅ Java 17+ Application Demo Completed Successfully!");
        System.out.println("==============================================================================");
    }
}
