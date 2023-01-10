buildscript {
    repositories {
        gradlePluginPortal()
    }

    dependencies {
        classpath(libs.moko.resources.generator)
        classpath(libs.sqldelight.gradlePlugin)
    }
}


allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
}
