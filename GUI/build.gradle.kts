
import buildutils.configureDetekt
import buildutils.createDetektTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.research.code.submissions.clustering.buildutils.configureDiktat
import org.jetbrains.research.code.submissions.clustering.buildutils.createDiktatTask

plugins {
    kotlin("jvm")
    id("com.github.gmazzo.buildconfig") version "3.0.3"
    id("org.jetbrains.compose") version "1.2.1"
    kotlin("plugin.serialization") version "1.7.20"
}

group = rootProject.group
version = rootProject.version
val mockkVersion = "1.13.2"

allprojects {
    apply {
        plugin("java")
        plugin("kotlin")
    }

    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://jitpack.io")
        google()
    }

    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
        implementation(compose.desktop.currentOs)
        implementation("io.ktor:ktor-client-core:2.1.3")
        implementation("io.ktor:ktor-client-websockets:2.1.3")
        implementation("io.ktor:ktor-client-cio:2.1.3")
        implementation("com.google.code.gson:gson:2.10")
        implementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
        runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
        implementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
        runtimeOnly("org.junit.platform:junit-platform-console:1.9.0")
        testImplementation("io.mockk:mockk:${mockkVersion}")
    }

    compose.desktop {
        application {
            mainClass = "MainKt"
        }
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    configureDiktat()
    configureDetekt()

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        description = "Runs detekt"
        setSource(files("src/main/kotlin", "src/test/kotlin"))
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(files("$projectDir/config/detekt.yml"))
        debug = true
        ignoreFailures = false
        reports {
            html.outputLocation.set(file("build/reports/detekt.html"))
        }
        include("**/*.kt")
        include("**/*.kts")
        exclude("resources/")
        exclude("build/")
    }
}

createDiktatTask()
createDetektTask()