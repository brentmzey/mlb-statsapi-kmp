# ⚾ MLB-StatsAPI Kotlin Multiplatform (KMP)

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-purple.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![Targets](https://img.shields.io/badge/Targets-JVM_|_Android_|_iOS_|_macOS_|_JS_|_TS-blue.svg)]()
[![Upstream Parity](https://img.shields.io/badge/Upstream_Parity-100%25_toddrob99%2FMLB--StatsAPI-green.svg)](https://github.com/toddrob99/MLB-StatsAPI)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

An asynchronous, strongly-typed **Kotlin Multiplatform (KMP)** SDK and wrapper for the official **Major League Baseball (MLB) Stats API** (`https://statsapi.mlb.com/api/v1/`).

This library is the Multiplatform Kotlin counterpart to the popular Python library [`toddrob99/MLB-StatsAPI`](https://github.com/toddrob99/MLB-StatsAPI), providing 1:1 endpoint parity, typed Kotlinx models, coroutine-based async/await, ASCII table formatters, and native bindings for **Java, Kotlin, Swift (iOS/macOS), and TypeScript/JavaScript**.

---

## 🌟 Key Features

* **Multiplatform Target Matrix**:
  * ☕ **JVM & Java 17+**: Synchronous blocking and `CompletableFuture` asynchronous methods via `MlbStatsApiJava`.
  * 🤖 **Android**: Native Android library with coroutines and LiveData/StateFlow support.
  * 🍎 **iOS & macOS**: Static Apple Framework with Swift async/await interop.
  * 🌐 **JavaScript & TypeScript**: Typed npm-compatible module with auto-generated `.d.ts` definitions.
* **1:1 Python API Parity**: Mirrors all `statsapi` methods (`schedule`, `standings`, `boxscore`, `linescore`, `player_stats`, `roster`, `lookup_player`, `lookup_team`, `get`).
* **Clean Terminal ASCII Tables**: Built-in formatters generating boxscores and linescores matching Python output.
* **🔄 Automated Upstream Synchronization**: Daily GitHub Actions workflow that tracks upstream updates in `toddrob99/MLB-StatsAPI` and keeps endpoints aligned.

---

## 📦 Installation

### Gradle (Kotlin Multiplatform / Android / JVM)

```kotlin
// build.gradle.kts
repositories {
    mavenCentral()
}

dependencies {
    // Multiplatform common
    implementation("com.sabermetrics.statsapi:mlb-statsapi-kmp:1.0.0")
}
```

### Maven (Java)

```xml
<dependency>
    <groupId>com.sabermetrics.statsapi</groupId>
    <artifactId>mlb-statsapi-kmp-jvm</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Swift (Swift Package Manager / iOS / macOS)

```swift
dependencies: [
    .package(url: "https://github.com/brentmzey/mlb-statsapi-kmp.git", from: "1.0.0")
]
```

---

## 🚀 Quickstart & Usage

### 1. Kotlin (Coroutines)

```kotlin
import com.sabermetrics.statsapi.MlbStatsApi

suspend fun main() {
    // 1. Fetch Today's Live Schedule
    val games = MlbStatsApi.schedule(date = "2026-08-28")
    for (game in games) {
        println("${game.awayName} (${game.awayScore}) @ ${game.homeName} (${game.homeScore}) [${game.status}]")
    }

    // 2. Fetch Division Standings
    val standings = MlbStatsApi.standings(season = 2026)
    standings.forEach { div ->
        println("=== ${div.divisionName} ===")
        div.teamRecords.forEach { t ->
            println("${t.teamName.padEnd(25)} ${t.wins}-${t.losses} (Diff: ${t.runDifferential})")
        }
    }

    // 3. Formatted ASCII Boxscore & Linescore
    val boxscoreText = MlbStatsApi.boxscore(gamePk = 745000L)
    println(boxscoreText)

    // 4. Lookup Players & Teams
    val ohtani = MlbStatsApi.lookupPlayer("Shohei Ohtani").first()
    println("Found player: ${ohtani.fullName} (#${ohtani.primaryNumber}) - Team: ${ohtani.currentTeamName}")
}
```

---

### 2. Java 17+ (Blocking & `CompletableFuture`)

```java
import com.sabermetrics.statsapi.java.MlbStatsApiJava;
import com.sabermetrics.statsapi.models.MlbScheduleGame;
import com.sabermetrics.statsapi.models.MlbDivisionStandings;
import java.util.List;

public class JavaExample {
    public static void main(String[] args) {
        // Synchronous blocking call
        List<MlbScheduleGame> games = MlbStatsApiJava.schedule("2026-08-28");
        games.forEach(g -> System.out.println(g.getSummary()));

        // Asynchronous CompletableFuture
        MlbStatsApiJava.standingsAsync(2026).thenAccept(standings -> {
            for (MlbDivisionStandings div : standings) {
                System.out.println("Division: " + div.getDivisionName());
            }
        });
    }
}
```

---

### 3. Swift (iOS & macOS)

```swift
import MlbStatsApi

Task {
    // Swift async / await bridging
    let games = try await MlbStatsApi.shared.schedule(date: "2026-08-28")
    for game in games {
        print("\(game.awayName) @ \(game.homeName): \(game.status)")
    }
}
```

---

### 4. TypeScript / JavaScript

```typescript
import { MlbStatsApi } from "mlb-statsapi-kmp";

async function run() {
    const games = await MlbStatsApi.schedule("2026-08-28");
    console.log(`Fetched ${games.length} games today.`);
}
run();
```

---

## 🧩 Method Parity Table with `toddrob99/MLB-StatsAPI`

| Python (`toddrob99/MLB-StatsAPI`) | Kotlin Multiplatform (`mlb-statsapi-kmp`) | Description |
| :--- | :--- | :--- |
| `statsapi.schedule(...)` | `MlbStatsApi.schedule(...)` | List of games for dates, teams, leagues |
| `statsapi.standings(...)` | `MlbStatsApi.standings(...)` | Division / Wild Card standings |
| `statsapi.boxscore(gamePk)` | `MlbStatsApi.boxscore(gamePk)` | Formatted ASCII boxscore table |
| `statsapi.boxscore_data(...)` | `MlbStatsApi.boxscoreData(...)` | Structured boxscore object model |
| `statsapi.linescore(gamePk)` | `MlbStatsApi.linescore(gamePk)` | Formatted linescore string |
| `statsapi.lookup_player(...)` | `MlbStatsApi.lookupPlayer(...)` | Player search by name / query |
| `statsapi.lookup_team(...)` | `MlbStatsApi.lookupTeam(...)` | Team search by name / abbreviation |
| `statsapi.roster(...)` | `MlbStatsApi.roster(...)` | Active / 40-man team roster |
| `statsapi.last_game(teamId)` | `MlbStatsApi.lastGame(teamId)` | Most recent completed gamePk |
| `statsapi.next_game(teamId)` | `MlbStatsApi.nextGame(teamId)` | Upcoming scheduled gamePk |
| `statsapi.get(endpoint, params)` | `MlbStatsApi.get(endpoint, params)` | Raw universal endpoint query pass-through |

---

## 🔄 Automated Upstream Synchronization

This repository includes a scheduled GitHub Action [`.github/workflows/upstream_sync.yml`](.github/workflows/upstream_sync.yml) that executes daily at `06:00 UTC`. It polls `toddrob99/MLB-StatsAPI`, analyzes endpoint additions in `statsapi/endpoints.py`, runs the multiplatform test suite, and opens automated pull requests whenever new API features are released upstream.

To run the parity check locally:
```bash
python3 scripts/sync_python_upstream.py
```

---

## 🧪 Testing

```bash
# Run all tests across JVM, JS, and Native
./gradlew check

# Run JVM Unit Tests & JaCoCo Coverage
./gradlew jvmTest jacocoTestReport
```

---

## 📄 License & Legal Disclaimer

* This project is licensed under the [MIT License](LICENSE).
* **Disclaimer**: This SDK is an independent open-source project and is **not affiliated with, endorsed by, or sponsored by Major League Baseball (MLB)** or any MLB franchise. Use of MLB Stats API data is subject to MLB's terms of service and data usage policies.
