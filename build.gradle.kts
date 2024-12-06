import org.danilopianini.gradle.mavencentral.JavadocJar

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotest.multiplatform) apply false
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.kotlin.qa)
    // alias(libs.plugins.multiJvmTesting)
    alias(libs.plugins.publishOnCentral)
    alias(libs.plugins.taskTree)
    id("com.android.application") version "8.0.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.22" apply false
}

val Provider<PluginDependency>.id get() = get().pluginId

allprojects {

    group = "it.nicolasfarabegoli.${rootProject.name}"

    with(rootProject.libs.plugins) {
        apply(plugin = kotlinx.serialization.id)
        apply(plugin = dokka.id)
        apply(plugin = gitSemVer.id)
        apply(plugin = kotlin.qa.id)
        apply(plugin = publishOnCentral.id)
        apply(plugin = taskTree.id)
    }

    repositories {
        google()
        mavenCentral()
    }

    tasks.dokkaJavadoc {
        enabled = false
    }

    tasks.withType<JavadocJar>().configureEach {
        val dokka = tasks.dokkaHtml.get()
        dependsOn(dokka)
        from(dokka.outputDirectory)
    }

    signing {
        if (System.getenv("CI") == "true") {
            val signingKey: String? by project
            val signingPassword: String? by project
            useInMemoryPgpKeys(signingKey, signingPassword)
        }
    }

    publishOnCentral {
        projectLongName.set("Crowd estimation using the pulverization")
        projectDescription.set("Crowd estimation using Android devices and pulverization")
        repository("https://maven.pkg.github.com/nicolasfara/${rootProject.name}".toLowerCase()) {
            user.set("nicolasfara")
            password.set(System.getenv("GITHUB_TOKEN"))
        }
        publishing {
            publications {
                withType<MavenPublication> {
                    pom {
                        scm {
                            connection.set("git:git@github.com:nicolasfara/${rootProject.name}")
                            developerConnection.set("git:git@github.com:nicolasfara/${rootProject.name}")
                            url.set("https://github.com/nicolasfara/${rootProject.name}")
                        }
                        developers {
                            developer {
                                name.set("Nicolas Farabegoli")
                                email.set("nicolas.farabegoli@gmail.com")
                                url.set("https://nicolasfarabegoli.it")
                            }
                        }
                    }
                }
            }
        }
    }

    publishing {
        publications {
            publications.withType<MavenPublication>().configureEach {
                if ("OSSRH" !in name) {
                    artifact(tasks.javadocJar)
                }
            }
        }
    }
}
