group = "xyz.xenondevs"
version = "1.2"

plugins {
    kotlin("jvm") version "1.8.20"
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("net.md-5:SpecialSource:1.11.0")
}

gradlePlugin {
    plugins {
        create("spigot") {
            id = "xyz.xenondevs.specialsource-gradle-plugin"
            displayName = "Gradle SpecialSource"
            description = "SpecialSource for Gradle to remap name definitions."
            implementationClass = "io.typecraft.gradlesource.SpecialSourceGradlePlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenKotlin") {
            from(components.getByName("kotlin"))
        }
    }

    repositories {
        maven {
            name = "xenondevs"
            url = uri("https://repo.xenondevs.xyz/releases/")
            credentials(PasswordCredentials::class)
        }
    }
}