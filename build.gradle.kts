plugins {
    alias(libs.plugins.kotlin.jvm)
    id("java-test-fixtures")
    alias(libs.plugins.git.version)
    alias(libs.plugins.ktfmt)
    `maven-publish`
}

kotlin {
    jvmToolchain(17)
}

ktfmt {
    kotlinLangStyle()
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


dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactive)

    implementation(libs.lettuce.core)

    //Logging
    implementation(libs.slf4j.api)

    testFixturesImplementation(libs.mockk)
    testFixturesImplementation(libs.kotlin.reflect)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.mockk)
    testImplementation(libs.assertk)
    testImplementation(libs.junit.jupiter.engine)
}

tasks {
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
