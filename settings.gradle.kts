pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    id("com.gradle.enterprise") version "3.12.6"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.1.5"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishOnFailure()
    }
}

gitHooks {
    commitMsg { conventionalCommits() }
    createHooks()
}

rootProject.name = "pulverization-crowd"
include(":estimator")
include(":common")
include(":app")
