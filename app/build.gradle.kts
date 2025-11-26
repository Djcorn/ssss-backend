plugins {
    application
    id("org.springframework.boot") version "3.5.7"
}

repositories {
    mavenCentral()
}

dependencies {

    // Import Testcontainers BOM
    implementation(platform("org.testcontainers:testcontainers-bom:1.20.0"))

    implementation("org.postgresql:postgresql:42.6.0")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.5.7")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc:3.5.7")
    implementation("org.springframework.boot:spring-boot-starter-jdbc:3.5.7")
    implementation("org.springframework.boot:spring-boot-starter-web:3.5.7")
    //implementation("org.springframework.boot:spring-boot-starter-security:3.5.7")
    //implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server:3.5.7")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testImplementation(libs.junit.jupiter)
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.5.7")
    testImplementation("org.testcontainers:testcontainers:2.0.2")
    testImplementation("org.testcontainers:postgresql:1.21.3")
    testImplementation("org.testcontainers:junit-jupiter:1.21.3")
    testImplementation("com.github.docker-java:docker-java:3.3.6")
    testImplementation("com.github.docker-java:docker-java-transport-httpclient5:3.3.6")
    testImplementation("javax.annotation:javax.annotation-api:1.3.2")
    testImplementation("javax.activation:javax.activation-api:1.2.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.2")

    implementation(libs.guava)
}

configurations.all {
    resolutionStrategy {
        force("com.github.docker-java:docker-java:3.3.0")
    }
}

application {
    mainClass = "s4.backend.App"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "s4.backend.App")
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}