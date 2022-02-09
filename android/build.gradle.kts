plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-android")
    id("kotlinx-serialization")
    id("androidx.navigation.safeargs.kotlin")
    id("dagger.hilt.android.plugin")
}

dependencies {
    val fragmentVersion = "1.3.6"

    implementation(project(":shared"))

    // Material
    implementation("com.google.android.material:material:1.5.0-alpha05")

    // Ktor
    implementation("io.ktor:ktor-client-android:1.5.0")
    implementation("io.ktor:ktor-client-serialization:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("io.ktor:ktor-client-logging-jvm:1.6.3")

    // App Compat
    implementation("androidx.appcompat:appcompat:1.3.1")

    // Constraint Layout
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")

    // Dagger - Hilt
    implementation("com.google.dagger:hilt-android:2.37")
    kapt("com.google.dagger:hilt-android-compiler:2.37")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    // Fragment
    implementation("androidx.fragment:fragment-ktx:$fragmentVersion")
    debugImplementation("androidx.fragment:fragment-testing:$fragmentVersion")

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")

    // Room
    implementation("androidx.room:room-runtime:2.4.0-beta01")
    implementation("androidx.room:room-ktx:2.4.0-beta01")
    kapt("androidx.room:room-compiler:2.4.0-beta01")

    // Support
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    // Logging
    implementation ("com.jakewharton.timber:timber:4.7.1")

    // Work
    val workVersion = "2.7.0"
    implementation ("androidx.work:work-runtime-ktx:$workVersion")
    implementation ("androidx.hilt:hilt-work:1.0.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.4-alpha03")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
}

kapt {
    correctErrorTypes = true
}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "com.ramitsuri.choresclient.android"
        minSdk = 26
        targetSdk = 31
        versionCode = 17
        versionName = "3.2"

        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}
