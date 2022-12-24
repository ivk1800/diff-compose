plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":vcs:api"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("com.google.code.gson:gson:2.10")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.7.10")

}
