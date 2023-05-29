apply(plugin = "kotlinx-serialization")

dependencies {
    api("com.nimbusds:nimbus-jose-jwt:9.26")
    api("org.http4k:http4k-connect-amazon-dynamodb")
    api("org.http4k:http4k-format-kotlinx-serialization")
    implementation("dev.forkhandles:result4k")
    implementation("dev.forkhandles:values4k")

    testImplementation("dev.mrbergin:result4k-kotest-matchers:2022.9.26")
}
