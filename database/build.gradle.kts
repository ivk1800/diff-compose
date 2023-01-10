plugins {
    kotlin("multiplatform")
    id("com.squareup.sqldelight")
}

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvm("desktop")
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.sqldelight.sqliteDriver)
            }
        }
    }
}

sqldelight {
    database("DiffDatabase") {
        packageName = "ru.ivk1800.diff.database"
    }
}
