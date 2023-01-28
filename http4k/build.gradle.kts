dependencies {
    api(project(":core"))

    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-contract")
    implementation("org.http4k:http4k-format-kotlinx-serialization")

    testImplementation("org.http4k:http4k-testing-kotest")
}
