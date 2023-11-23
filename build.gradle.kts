plugins {
    kotlin("jvm") version("1.9.20")
    id("com.expediagroup.graphql").version("7.0.2")
    application
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
    implementation("com.expediagroup:graphql-kotlin-ktor-server:7.0.2")
    implementation("io.ktor:ktor-server-netty:2.3.6")
    implementation("ch.qos.logback:logback-classic:1.4.11")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

graphql {
    schema {
        packages = listOf("io.humla.graphql")
    }
}
