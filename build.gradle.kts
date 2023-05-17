plugins {
    java
    id("org.springframework.boot") version "3.0.2"
    id("io.spring.dependency-management") version "1.1.0"
}

group = "com.github.msemitkin.financie"
version = "1.2.0"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.flywaydb:flyway-core")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("org.telegram:telegrambots-spring-boot-starter:6.5.0")

    implementation("com.opencsv:opencsv:5.7.1")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")

    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("com.google.maps:google-maps-services:2.2.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation(platform("org.testcontainers:testcontainers-bom:1.17.6"))
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:postgresql")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
