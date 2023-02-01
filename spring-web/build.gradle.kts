plugins {
    kotlin("plugin.spring") version "1.8.0"
    id("org.springframework.boot") version "2.7.8"
    id("io.spring.dependency-management") version "1.1.0"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    api(project(":core"))


    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
