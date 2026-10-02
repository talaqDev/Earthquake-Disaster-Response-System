import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("java")
    application
}

group = "edu.trinity.cpsc215"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.6")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.6")

    testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
        events("standardOut", "started", "passed", "skipped", "failed")
    }
}

application {
    mainClass = "edu.trinity.cpsc215.disaster.App"
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

distributions {
    main {
        contents {
            from(sourceSets.main.get().allSource) {
                into("src/main")
            }
            from(sourceSets.test.get().allSource) {
                into("src/test")
            }
        }
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "edu.trinity.cpsc215.disaster.App"
    }
}

tasks.register<JavaExec>("generateData") {
    description = "Generate synthetic earthquake disaster datasets"
    group = "application"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "edu.trinity.cpsc215.disaster.generator.DataGenerator"
}

tasks.register<JavaExec>("map") {
    description = "Serve the map visualization in a browser"
    group = "application"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "edu.trinity.cpsc215.disaster.io.MapServer"
}

tasks.register<Zip>("studentZip") {
    description = "Build the student distribution zip with skeleton stubs"
    group = "distribution"

    archiveFileName = "disaster-response-project.zip"
    destinationDirectory = layout.buildDirectory.dir("distributions")

    val projectDir = layout.projectDirectory
    val skeletonStructures = "skeleton/src/main/java/edu/trinity/cpsc215/disaster/structures"
    val skeletonEngine = "skeleton/src/main/java/edu/trinity/cpsc215/disaster/engine"
    val srcBase = "src/main/java/edu/trinity/cpsc215/disaster"

    // Gradle wrapper
    from(projectDir.dir("gradle")) { into("disaster-response/gradle") }
    from(projectDir.file("gradlew")) { into("disaster-response") }
    from(projectDir.file("gradlew.bat")) { into("disaster-response") }

    // Build files
    from(projectDir.file("build.gradle.kts")) { into("disaster-response") }
    from(projectDir.file("settings.gradle.kts")) { into("disaster-response") }

    // Student instructions
    from(projectDir.file("instructions.md")) { into("disaster-response") }

    // Source: model, io, App (provided as-is)
    from(projectDir.dir("$srcBase/model")) { into("disaster-response/$srcBase/model") }
    from(projectDir.dir("$srcBase/io")) { into("disaster-response/$srcBase/io") }
    from(projectDir.file("$srcBase/App.java")) { into("disaster-response/$srcBase") }

    // Source: skeleton stubs (NOT the reference solution)
    from(projectDir.dir(skeletonStructures)) { into("disaster-response/$srcBase/structures") }
    from(projectDir.file("$skeletonEngine/OrganismFilter.java")) { into("disaster-response/$srcBase/engine") }
    from(projectDir.file("$skeletonEngine/DnaComparator.java")) { into("disaster-response/$srcBase/engine") }
    from(projectDir.file("$skeletonEngine/DisasterZoneCalculator.java")) { into("disaster-response/$srcBase/engine") }
    from(projectDir.file("$skeletonEngine/MatchEngine.java")) { into("disaster-response/$srcBase/engine") }

    // Tests (all provided)
    from(projectDir.dir("src/test")) { into("disaster-response/src/test") }

    // Resources: data files (no results.json) + map
    from(projectDir.dir("src/main/resources/data")) {
        into("disaster-response/src/main/resources/data")
        exclude("results.json")
    }
    from(projectDir.dir("src/main/resources/map")) { into("disaster-response/src/main/resources/map") }

    // Excludes: no generator, no skeleton/, no ta-readme, no results.json
    doLast {
        println()
        println("Student zip created: ${archiveFile.get().asFile}")
        println()
    }
}
