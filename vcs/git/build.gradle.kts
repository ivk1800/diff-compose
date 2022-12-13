plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":vcs:api"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}
