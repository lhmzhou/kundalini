import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    kotlin("jvm") version "1.3.72" apply false
}

buildscript {
    dependencies {
        classpath("de.dynamicfiles.projects.gradle.plugins", "javafx-gradle-plugin", "8.8.2")
        classpath("com.github.jengelman.gradle.plugins", "shadow", "5.2.0")
    }

    repositories {
        jcenter()
        mavenCentral()
    }
}

allprojects {
    group = "com.kundalini"

    version = "1.0"

    repositories {
        jcenter()
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

