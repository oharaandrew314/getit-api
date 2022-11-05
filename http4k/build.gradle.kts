dependencies {
    api(project(":core"))

    implementation("com.github.oharaandrew314:service-utils:0.9.0")
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-contract")
    implementation("org.http4k:http4k-format-moshi") {
        exclude("com.squareup.moshi", "moshi-kotlin")
    }

    testImplementation("org.http4k:http4k-testing-kotest")
}
