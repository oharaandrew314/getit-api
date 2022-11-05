dependencies {
    implementation("com.nimbusds:nimbus-jose-jwt:9.25.6")
    implementation("dev.zacsweers.moshix:moshi-metadata-reflect:0.19.0")
    api("org.http4k:http4k-connect-amazon-dynamodb")
    implementation("dev.forkhandles:result4k")
    implementation("dev.forkhandles:values4k")

    testImplementation("dev.mrbergin:result4k-kotest-matchers:2022.9.26")
}
