plugins {
    java
    id("org.springframework.boot") version "3.2.4"
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
//    implementation("com.amazonaws.serverless:aws-serverless-java-container-springboot3:2.0.0")
    implementation("com.amazonaws.serverless:aws-serverless-java-container-springboot3:2.0.0-M2")

    implementation("org.crac:crac:1.4.0")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("org.telegram:telegrambots-client:7.0.0-rc0")
    implementation("org.telegram:telegrambots-springboot-webhook-starter:7.0.0-rc0")

    implementation("com.opencsv:opencsv:5.7.1")


    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("com.google.maps:google-maps-services:2.2.0")
    implementation("javax.xml.bind:jaxb-api:2.3.1")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.17.6"))
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:postgresql")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher") // fix https://github.com/spring-io/initializr/issues/1476
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    wrapper {
        gradleVersion = "8.5"
        distributionType = Wrapper.DistributionType.ALL
    }
}
