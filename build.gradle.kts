import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "1.7.20"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "kover")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    repositories {
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
    }

    dependencies {
        implementation(platform("io.ktor:ktor-bom:2.1.3"))
        implementation(platform("org.http4k:http4k-bom:4.33.3.0"))
        implementation(platform("org.http4k:http4k-connect-bom:3.24.0.0"))
        implementation(platform("dev.forkhandles:forkhandles-bom:2.3.0.0"))

        implementation(kotlin("stdlib-jdk8"))
        implementation("org.slf4j:slf4j-simple:2.0.3")

        testImplementation(platform("io.kotest:kotest-bom:5.5.4"))

        testImplementation("io.kotest:kotest-runner-junit5-jvm:5.5.4")
        testImplementation("io.kotest:kotest-assertions-core-jvm")
        testImplementation("org.http4k:http4k-connect-amazon-dynamodb-fake")
        testImplementation(kotlin("test"))
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.check {
        dependsOn(tasks.ktlintCheck)
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        reporters {
            reporter(ReporterType.CHECKSTYLE)
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML)
        }
    }

    kover {
        verify {
            onCheck.set(true)
            rule {
                bound {
                    minValue = 80
                }
            }
        }
    }
}

dependencies {
    implementation(project("http4k"))
    compileOnly(project(":ktor"))

    implementation("org.http4k:http4k-cloudnative")
    implementation("org.http4k:http4k-serverless-lambda")

    testImplementation(project(":ktor"))
    testImplementation("io.ktor:ktor-server-netty")
}

kover {
    verify {
        isDisabled.set(true)
    }
}
