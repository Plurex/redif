plugins {
    kotlin("jvm") version "1.5.10"
    id("java-test-fixtures")
    id("com.palantir.git-version") version "0.12.3"
    `maven-publish`
}

val gitVersion: groovy.lang.Closure<String> by extra

group = "io.plurex"
version = gitVersion().replace(".dirty", "")

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven {
        url = uri("https://plurex-980945093956.d.codeartifact.eu-west-1.amazonaws.com/maven/plurex.prod/")
        credentials {
            username = "aws"
            password = System.getenv("CODEARTIFACT_PASSWORD")
        }
    }
}

val kotlinStdlibVersion: String by project
val kotlinVersion: String by project
val kotlinXVersion: String by project
val slf4jVersion: String by project
val lettuceVersion: String by project
val pangolinVersion: String by project

val junitJupiterVersion: String by project
val mockkVersion: String by project
val assertKVersion: String by project

dependencies {
    implementation(kotlin(kotlinStdlibVersion))
    implementation("org.jetbrains.kotlin", "kotlin-reflect", kotlinVersion)
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", kotlinXVersion)
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-reactive", kotlinXVersion)

    implementation("io.lettuce:lettuce-core:$lettuceVersion")

    //Logging
    implementation("org.slf4j", "slf4j-api", slf4jVersion)

    implementation("io.plurex", "pangolin", pangolinVersion)

    testFixturesImplementation("io.mockk", "mockk", mockkVersion)
    testFixturesImplementation("org.jetbrains.kotlin", "kotlin-reflect", kotlinVersion)

    testImplementation("org.junit.jupiter", "junit-jupiter-api", junitJupiterVersion)
    testImplementation("org.junit.jupiter", "junit-jupiter-params", junitJupiterVersion)
    testImplementation("io.mockk", "mockk", mockkVersion)
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertKVersion")
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", junitJupiterVersion)
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi")
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.serialization.ExperimentalSerializationApi")
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlin.ExperimentalUnsignedTypes")
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.coroutines.ObsoleteCoroutinesApi")
        kotlinOptions.freeCompilerArgs += listOf("-Xopt-in=io.ktor.util.KtorExperimentalAPI")
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi")
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.serialization.ExperimentalSerializationApi")
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlin.ExperimentalUnsignedTypes")
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.coroutines.ObsoleteCoroutinesApi")

    }
    compileTestFixturesKotlin {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi")
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.serialization.ExperimentalSerializationApi")
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlin.ExperimentalUnsignedTypes")
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.coroutines.ObsoleteCoroutinesApi")

    }

    val composeUp = task<Exec>("composeUp") {
        executable = "docker"
        setArgs(listOf("compose", "-f", "$rootDir/docker-compose.yml", "up", "-d"))
    }

    test {
        dependsOn(composeUp)
        useJUnitPlatform()
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

publishing {
    publications {
        create<MavenPublication>("plurexRedif") {
            groupId = "io.plurex"
            artifactId = "redif"
            version = version
            from(components["java"])
            artifact(sourcesJar)
        }
    }
    repositories {
        maven {
            url = uri("https://plurex-980945093956.d.codeartifact.eu-west-1.amazonaws.com/maven/plurex.prod/")
            credentials {
                username = "aws"
                password = System.getenv("CODEARTIFACT_PASSWORD")
            }
        }
    }
}
