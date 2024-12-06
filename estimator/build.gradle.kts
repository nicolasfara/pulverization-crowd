plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version "1.4.2"
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(project(":common"))
    implementation(libs.bundles.pulverization)
}
