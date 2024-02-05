plugins {
    java
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.github.msemitkin.financie"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.flywaydb:flyway-core")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("org.telegram:telegrambots-spring-boot-starter:6.9.7.0")

    implementation("com.opencsv:opencsv:5.7.1")


    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("com.google.maps:google-maps-services:2.2.0")
    implementation("javax.xml.bind:jaxb-api:2.3.1")

    runtimeOnly("org.postgresql:postgresql")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher") // fix https://github.com/spring-io/initializr/issues/1476

    testImplementation(platform("org.testcontainers:testcontainers-bom:1.17.6"))
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:postgresql")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    jar {
        enabled = false
    }

    wrapper {
        gradleVersion = "8.5"
        distributionType = Wrapper.DistributionType.ALL
    }
}
