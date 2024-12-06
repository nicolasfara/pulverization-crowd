plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

repositories {
    google()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

android {
    namespace = "it.nicolasfarabegoli.crowd"
    compileSdk = 33

    packagingOptions {
        resources.excludes += "META-INF/*.md"
    }

    defaultConfig {
        applicationId = "it.nicolasfarabegoli.crowd"
        minSdk = 30
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.coroutine.core)
    implementation(libs.coroutine.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.containtlayout)
    implementation(libs.androidx.lifecycle)
    implementation(libs.material)
    implementation(libs.pulverization.core)
    implementation(libs.pulverization.platform)
    implementation(libs.pulverization.rabbitmq)
    implementation(libs.koin.core)
    implementation(libs.beacon)
    implementation(project(":common"))
    implementation("com.github.weliem:blessed-android-coroutines:0.4.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
