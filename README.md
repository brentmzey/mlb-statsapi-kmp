# ⚾ MLB-StatsAPI Kotlin Multiplatform (KMP) + Arrow KT Functional

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-purple.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![Arrow KT](https://img.shields.io/badge/Arrow_KT-Functional_Monads_Either_Option-blueviolet.svg)](https://arrow-kt.io/)
[![Targets](https://img.shields.io/badge/Targets-JVM_|_Android_|_iOS_|_macOS_|_JS_|_TS-blue.svg)]()
[![Upstream Parity](https://img.shields.io/badge/Upstream_Parity-100%25_toddrob99%2FMLB--StatsAPI-green.svg)](https://github.com/toddrob99/MLB-StatsAPI)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

An asynchronous, **purely functional**, strongly-typed **Kotlin Multiplatform (KMP)** SDK and wrapper for the official **Major League Baseball (MLB) Stats API** (`https://statsapi.mlb.com/api/v1/`).

This library is the Multiplatform Kotlin counterpart to Python's [`toddrob99/MLB-StatsAPI`](https://github.com/toddrob99/MLB-StatsAPI), enhanced with **Arrow KT** monadic error handling (`Either<MlbStatsError, T>`, `Raise` DSL, `Option`), typed Kotlinx models, coroutine-based async/await, ASCII table formatters, and native bindings for **Java 17+ (`Optional`), Swift (iOS/macOS), and TypeScript/JavaScript**.

---

## 🌟 Key Features

* **Functional Monadic Error Handling (Arrow KT)**:
  * Pure algebraic error hierarchy via `sealed interface MlbStatsError` (`NetworkError`, `HttpError`, `ParsingError`, `EntityNotFoundError`, `InvalidParameterError`).
  * Explicit monadic return types: `Either<MlbStatsError, T>`.
  * Arrow 1.x / 2.x `either { ... }` computation blocks with `.bind()`.
  * Monadic combinators: `map`, `flatMap`, `fold`, `getOrElse`, `recover`, `zip`, `traverse`.
* **Multiplatform Target Matrix**:
  * ☕ **JVM & Java 17+**: Functional `Optional.ofNullable`, `Either` bridges, and `CompletableFuture` asynchronous methods via `MlbStatsApiJava`.
  * 🤖 **Android**: Native Android library with coroutines and `StateFlow` streams.
  * 🍎 **iOS & macOS**: Apple Framework with Swift async/await interop.
  * 🌐 **JavaScript & TypeScript**: Typed npm-compatible module with auto-generated `.d.ts` definitions.
* **1:1 Python API Parity**: Full coverage of all 77+ endpoints from `toddrob99/MLB-StatsAPI`.
* **🔄 Automated Upstream Synchronization**: Daily GitHub Actions workflow that tracks upstream updates in `toddrob99/MLB-StatsAPI` and verifies endpoint parity.

---

## 📦 Installation

### Gradle (Kotlin Multiplatform / Android / JVM)

```kotlin
// build.gradle.kts
repositories {
    mavenCentral()
}

dependencies {
    // Multiplatform common with Arrow KT functional core
    implementation("com.sabermetrics.statsapi:mlb-statsapi-kmp:1.0.0")
    implementation("io.arrow-kt:arrow-core:1.2.4")
}
```

### Maven (Java 17+)

```xml
<dependency>
    <groupId>com.sabermetrics.statsapi</groupId>
    <artifactId>mlb-statsapi-kmp-jvm</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 🚀 Functional Programming Quickstart

### 1. Arrow KT Functional Pipeline (`Either`, `fold`, `map`, `flatMap`)

```kotlin
import arrow.core.*
import arrow.core.raise.either
import com.sabermetrics.statsapi.MlbStatsApi
import com.sabermetrics.statsapi.error.MlbStatsError
import com.sabermetrics.statsapi.models.MlbScheduleGame

suspend fun main() {
    // 1. Monadic Error Handling with Arrow Either & Fold
    val scheduleResult: Either<MlbStatsError, List<MlbScheduleGame>> = 
        MlbStatsApi.scheduleEither(date = "2026-08-28")

    scheduleResult.fold(
        ifLeft = { error ->
            println("❌ Pipeline failed with domain error: ${error.message}")
        },
        ifRight = { games ->
            println("✅ Fetched ${games.size} games successfully:")
            games.forEach { g -> println("   • ${g.awayName} @ ${g.homeName}") }
        }
    )

    // 2. Monadic Composition with Arrow `either { ... }` and `.bind()`
    val divisionSummary: Either<MlbStatsError, String> = either {
        val standings = MlbStatsApi.standingsEither(season = 2026).bind()
        val nlWest = standings.firstOrNull { it.divisionName.contains("West") }
            ?: raise(MlbStatsError.EntityNotFoundError("Division", "NL West"))
        
        val leader = nlWest.teamRecords.maxByOrNull { it.winPct }
            ?: raise(MlbStatsError.EntityNotFoundError("Team", "Division Leader"))
        
        "NL West Leader: ${leader.teamName} (${leader.wins}-${leader.losses})"
    }

    println(divisionSummary.getOrElse { "Could not evaluate standings" })
}
```

---

### 2. Java 17+ Interop (`Optional.ofNullable`, `Optional.map`, `Optional.flatMap`)

```java
import com.sabermetrics.statsapi.java.MlbStatsApiJava;
import com.sabermetrics.statsapi.models.MlbPlayerLookup;
import java.util.Optional;

public class JavaFunctionalExample {
    public static void main(String[] args) {
        // Monadic Optional chain
        Optional<String> playerSummary = MlbStatsApiJava.lookupFirstPlayer("Shohei Ohtani")
            .filter(MlbPlayerLookup::getActive)
            .map(p -> p.getFullName() + " plays for team ID: " + p.getCurrentTeamId());

        playerSummary.ifPresentOrElse(
            System.out::println,
            () -> System.out.println("Player not found or inactive")
        );

        // Asynchronous CompletableFuture
        MlbStatsApiJava.standingsAsync(2026).thenAccept(standings -> {
            standings.stream()
                .flatMap(div -> div.getTeamRecords().stream())
                .filter(t -> t.getRunDifferential() > 50)
                .forEach(t -> System.out.println("Contender: " + t.getTeamName()));
        });
    }
}
```

---

### 3. Swift (iOS & macOS)

```swift
import MlbStatsApi

Task {
    // Swift async/await
    let games = try await MlbStatsApi.shared.schedule(date: "2026-08-28")
    for game in games {
        print("\(game.awayName) @ \(game.homeName): \(game.status)")
    }
}
```

---

### 4. TypeScript / JavaScript (Node.js & Web)

```typescript
import { MlbStatsApi } from "mlb-statsapi-kmp";

async function main() {
    const standings = await MlbStatsApi.standings(2026);
    const positiveDiffTeams = standings
        .flatMap(div => div.teamRecords)
        .filter(t => t.runDifferential > 0)
        .map(t => `${t.teamName}: +${t.runDifferential}`);
    
    console.log("Positive Run Differential Teams:", positiveDiffTeams);
}
main();
```

---

## 🧩 Algebraic Domain Error Hierarchy

```kotlin
sealed interface MlbStatsError {
    val message: String

    data class NetworkError(val message: String, val url: String, val causeMessage: String? = null) : MlbStatsError
    data class HttpError(val statusCode: Int, val statusText: String, val url: String, val responseBody: String = "") : MlbStatsError
    data class ParsingError(val message: String, val rawSnippet: String = "", val causeMessage: String? = null) : MlbStatsError
    data class EntityNotFoundError(val entityType: String, val identifier: String) : MlbStatsError
    data class InvalidParameterError(val parameter: String, val reason: String) : MlbStatsError
    data class UpstreamApiError(val message: String, val errorCode: String? = null) : MlbStatsError
}
```

---

## 🔄 Automated Upstream Synchronization

This repository includes a scheduled GitHub Action [`.github/workflows/upstream_sync.yml`](.github/workflows/upstream_sync.yml) executing daily at `06:00 UTC` (`01:00 AM Central`). It monitors `toddrob99/MLB-StatsAPI`, verifies endpoint additions in `statsapi/endpoints.py`, runs the multiplatform test suite, and opens automated pull requests.

```bash
cd ~/personal/mlb-statsapi-kmp
python3 scripts/sync_python_upstream.py
```

---

## 🏃 Native Runnable Applications & Demos

You can pull and immediately execute the native applications across all 4 programming environments:

### 1. Kotlin Multiplatform CLI Application
```bash
./gradlew run
```
*Fetches live schedule, 2026 MLB division standings, and runs player lookups.*

### 2. Pure Java 17+ Application Demo
```bash
./gradlew runJava
```
*Executes `MlbStatsApiJava` using Java `Optional.ofNullable`, Stream `.flatMap()`, and `.filter()`.*

### 3. Node.js Native Application
```bash
node examples/nodejs/demo.js
```
*Executes JavaScript async/await and functional array transformations.*

### 4. Swift / Apple Native Application
```bash
swift examples/swift/demo.swift
```
*Executes Swift `URLSession` async/await and functional filters.*

---

## 🧪 Testing

```bash
# Run all Multiplatform verification suites (JVM, Java 17+, JS Browser, JS Node)
./gradlew check jacocoTestReport

# Run Swift SPM verification harness
./scripts/verify_swift_interop.sh
```

---

## 📄 License

* [MIT License](LICENSE)
* **Disclaimer**: This SDK is an independent open-source project and is **not affiliated with, endorsed by, or sponsored by Major League Baseball (MLB)**.
