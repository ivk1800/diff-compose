plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":vcs:api"))
    implementation(project(":logger:api"))
    implementation(libs.kotlinx.coroutines.core)
}
