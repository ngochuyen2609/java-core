plugins {
    kotlin("jvm") version "1.8.10"
}

group = "ngochuyen"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    //UniTest
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter");

    //sql
    // https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client
    implementation("org.mariadb.jdbc:mariadb-java-client:3.4.1")
    // https://mvnrepository.com/artifact/com.zaxxer/HikariCP
    implementation("com.zaxxer:HikariCP:4.0.3")

    //gson
    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.10.1")
    implementation ("com.fasterxml.jackson.core:jackson-databind:2.14.0")

    //ma hoa
    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    implementation("org.apache.commons:commons-lang3:3.12.0")
    // https://mvnrepository.com/artifact/commons-codec/commons-codec
    implementation("commons-codec:commons-codec:1.17.1")
}

tasks.test {
    useJUnitPlatform()
}