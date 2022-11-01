import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "1.7.20"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
}

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.slf4j:slf4j-simple:2.0.3")
    implementation("com.github.oharaandrew314:service-utils:0.8.4")
    implementation("com.nimbusds:nimbus-jose-jwt:9.25.6")
    implementation("dev.zacsweers.moshix:moshi-metadata-reflect:0.19.0")

    // http4k
    implementation(platform("org.http4k:http4k-bom:4.33.1.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-serverless-lambda")
    implementation("org.http4k:http4k-contract")
    implementation("org.http4k:http4k-cloudnative")
    implementation("org.http4k:http4k-format-moshi") {
        exclude("com.squareup.moshi", "moshi-kotlin")
    }
    testImplementation("org.http4k:http4k-testing-kotest")

    // http4k-connect
    implementation(platform("org.http4k:http4k-connect-bom:3.23.0.0"))
    implementation("org.http4k:http4k-connect-amazon-dynamodb")
    testImplementation("org.http4k:http4k-connect-amazon-dynamodb-fake")

    // forkhandles
    implementation(platform("dev.forkhandles:forkhandles-bom:2.2.0.0"))
    implementation("dev.forkhandles:result4k")
    implementation("dev.forkhandles:values4k")
    testImplementation("dev.mrbergin:result4k-kotest-matchers:2022.9.26")

    // kotest
    testImplementation(platform("io.kotest:kotest-bom:5.5.2"))
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.5.2")
    testImplementation("io.kotest:kotest-assertions-core-jvm")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
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
