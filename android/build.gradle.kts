plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-android")
    id("kotlinx-serialization")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

dependencies {
    val fragmentVersion = "1.3.6"

    implementation(project(":shared"))
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")

    // Material
    implementation("com.google.android.material:material:1.5.0-alpha05")

    // App Compat
    implementation("androidx.appcompat:appcompat:1.3.1")

    // Constraint Layout
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")

    // Date Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")

    // Fragment
    implementation("androidx.fragment:fragment-ktx:$fragmentVersion")
    debugImplementation("androidx.fragment:fragment-testing:$fragmentVersion")

    // Koin
    val koin = findProperty("version.koin")
    implementation("io.insert-koin:koin-android:$koin")
    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")

    // Support
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    // Work
    val workVersion = "2.7.0"
    implementation("androidx.work:work-runtime-ktx:$workVersion")

    // Compose
    implementation ("androidx.compose.ui:ui:1.1.1")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.1.1")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
    implementation ("androidx.activity:activity-compose:1.4.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.1")
    implementation ("androidx.compose.material3:material3:1.0.0-alpha13")
    implementation ("androidx.compose.material3:material3-window-size-class:1.0.0-alpha13")
    implementation ("com.google.accompanist:accompanist-flowlayout:0.24.8-beta")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.4-alpha03")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
}

kapt {
    correctErrorTypes = true
}

android {
    compileSdk = 32
    defaultConfig {
        applicationId = "com.ramitsuri.choresclient.android"
        minSdk = 26
        targetSdk = 32
        versionCode = 29
        versionName = "4.4"

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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.1"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
