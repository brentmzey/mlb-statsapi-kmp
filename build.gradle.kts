plugins {
    kotlin("multiplatform") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
    idea
    jacoco
}

group = "com.sabermetrics.statsapi"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
}

val ktorVersion = "2.3.12"
val coroutinesVersion = "1.8.1"
val serializationVersion = "1.6.3"
val dateTimeVersion = "0.6.0"
val arrowVersion = "1.2.4"

val isXcodeAvailable = file("/Applications/Xcode.app").exists() || System.getenv("GITHUB_ACTIONS") == "true"

kotlin {
    jvmToolchain(17)
    applyDefaultHierarchyTemplate()

    // 1. JVM Target (with Java Interop support)
    jvm {
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    // 2. JS / TypeScript Target
    js(IR) {
        browser()
        nodejs()
    }

    // 3. Apple Native Targets (iOS & macOS - Enabled when Xcode is available)
    if (isXcodeAvailable) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
        macosX64()
        macosArm64()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("stdlib"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:$dateTimeVersion")
            implementation("io.arrow-kt:arrow-core:$arrowVersion")
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            implementation("io.ktor:ktor-client-logging:$ktorVersion")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
            implementation("io.ktor:ktor-client-mock:$ktorVersion")
        }

        jvmMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
            implementation("org.slf4j:slf4j-api:2.0.13")
            implementation("ch.qos.logback:logback-classic:1.4.14")
        }

        jvmTest.dependencies {
            implementation(kotlin("test-junit5"))
            implementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
            implementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
        }

        jsMain.dependencies {
            implementation("io.ktor:ktor-client-js:$ktorVersion")
        }

        if (isXcodeAvailable) {
            appleMain.dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            }
        }
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Generates JaCoCo test coverage report."
    dependsOn("jvmTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    val jvmTarget = kotlin.targets.getByName("jvm") as org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
    val mainCompilation = jvmTarget.compilations.getByName("main")
    classDirectories.setFrom(mainCompilation.output.classesDirs)
    sourceDirectories.setFrom(files("src/commonMain/kotlin", "src/jvmMain/kotlin"))
    executionData.setFrom(fileTree(layout.buildDirectory.dir("jacoco")).matching { include("*.exec") })
}

tasks.register<JavaExec>("run") {
    group = "application"
    description = "Runs the Kotlin Multiplatform MLB-StatsAPI CLI."
    val jvmTarget = kotlin.targets.getByName("jvm") as org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
    val mainCompilation = jvmTarget.compilations.getByName("main")
    classpath = mainCompilation.output.allOutputs + mainCompilation.runtimeDependencyFiles
    mainClass.set("com.sabermetrics.statsapi.cli.MlbStatsCli")
}

tasks.register<JavaExec>("runJava") {
    group = "application"
    description = "Runs the Pure Java 17+ Application Demo."
    val jvmTarget = kotlin.targets.getByName("jvm") as org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
    val mainCompilation = jvmTarget.compilations.getByName("main")
    classpath = mainCompilation.output.allOutputs + mainCompilation.runtimeDependencyFiles
    mainClass.set("com.sabermetrics.statsapi.demo.MlbJavaApp")
}

