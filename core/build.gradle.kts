apply(plugin = "kotlinx-serialization")

dependencies {
    implementation("com.nimbusds:nimbus-jose-jwt:9.26")
    api("org.http4k:http4k-connect-amazon-dynamodb")
    implementation("dev.forkhandles:result4k")
    implementation("dev.forkhandles:values4k")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")

    testImplementation("dev.mrbergin:result4k-kotest-matchers:2022.9.26")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
}
