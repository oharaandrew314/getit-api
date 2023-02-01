import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "1.8.0"
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
        implementation(platform("io.ktor:ktor-bom:2.2.1"))
        implementation(platform("org.http4k:http4k-bom:4.37.0.0"))
        implementation(platform("org.http4k:http4k-connect-bom:3.27.1.0"))
        implementation(platform("dev.forkhandles:forkhandles-bom:2.3.0.0"))

        implementation(kotlin("stdlib-jdk8"))
        implementation("org.slf4j:slf4j-simple:2.0.6")
        implementation("com.github.oharaandrew314:service-utils:0.9.0")

        testImplementation("io.kotest:kotest-assertions-core-jvm:5.5.4")
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
}

subprojects {
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
    implementation("org.http4k:http4k-serverless-lambda")
}

koverMerged {
    enable()
}
