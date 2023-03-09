import org.danilopianini.gradle.mavencentral.JavadocJar
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.gradle.internal.os.OperatingSystem

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.publishOnCentral)
    alias(libs.plugins.taskTree)
}

group = "it.nicolasfarabegoli.${rootProject.name}"

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.bundles.pulverization)
                implementation(libs.bundles.coroutines)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.bundles.kotlin.testing.common)
                implementation(libs.bundles.kotest.common)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.runner.junit5)
            }
        }
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }
    }

    js(IR) {
        browser()
        nodejs()
        binaries.library()
    }

    val nativeSetup: KotlinNativeTarget.() -> Unit = {
        compilations["main"].defaultSourceSet.dependsOn(kotlin.sourceSets["nativeMain"])
        compilations["test"].defaultSourceSet.dependsOn(kotlin.sourceSets["nativeTest"])
        binaries {
            sharedLib()
            staticLib()
        }
    }

    linuxX64(nativeSetup)
    // linuxArm64(nativeSetup)

    mingwX64(nativeSetup)

    macosX64(nativeSetup)
    macosArm64(nativeSetup)
    ios(nativeSetup)
    watchos(nativeSetup)
    tvos(nativeSetup)

    targets.all {
        compilations.all {
            kotlinOptions {
                allWarningsAsErrors = true
            }
        }
    }

    // Disable cross compilation
    val os = OperatingSystem.current()
    val excludeTargets = when {
        os.isLinux -> kotlin.targets.filter { "linux" !in it.name }
        os.isWindows -> kotlin.targets.filter { "mingw" !in it.name }
        os.isMacOsX -> kotlin.targets.filter { "linux" in it.name || "mingw" in it.name }
        else -> emptyList()
    }.mapNotNull { it as? KotlinNativeTarget }

    configure(excludeTargets) {
        compilations.all {
            cinterops.all { tasks[interopProcessingTaskName].enabled = false }
            compileTaskProvider.get().enabled = false
            tasks[processResourcesTaskName].enabled = false
        }
        binaries.all { linkTask.enabled = false }

        mavenPublication {
            val publicationToDisable = this
            tasks.withType<AbstractPublishToMaven>()
                .all { onlyIf { publication != publicationToDisable } }
            tasks.withType<GenerateModuleMetadata>()
                .all { onlyIf { publication.get() != publicationToDisable } }
        }
    }
}
