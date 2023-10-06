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
    implementation(project(":shared"))
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    // Material
    implementation("com.google.android.material:material:1.11.0-alpha02")

    // App Compat
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Date Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    // Koin
    val koin = findProperty("version.koin")
    implementation("io.insert-koin:koin-android:$koin")
    implementation("io.insert-koin:koin-androidx-compose:$koin")

    // Work
    val workVersion = "2.9.0-alpha02"
    implementation("androidx.work:work-runtime-ktx:$workVersion")

    // Compose
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")
    implementation("androidx.compose.ui:ui-tooling:1.6.0-alpha04")
    implementation("androidx.compose.material:material:1.5.0")
    implementation("androidx.compose.material:material-icons-extended:1.5.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-process:2.6.1")
    implementation("androidx.compose.material3:material3:1.2.0-alpha06")
    implementation("androidx.compose.material3:material3-window-size-class:1.2.0-alpha06")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.28.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.28.0")
    // 2.5.1 is causing a crash - List contains no element matching the predicate
    implementation("androidx.navigation:navigation-compose:2.7.1")
    implementation("io.github.vanpra.compose-material-dialogs:datetime:0.9.0")

    // Firebase
    implementation(project.dependencies.platform("com.google.firebase:firebase-bom:32.2.3"))
    implementation("com.google.firebase:firebase-messaging-ktx")

    implementation("androidx.core:core-splashscreen:1.0.0-beta02")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.2.0-alpha01")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
}

kapt {
    correctErrorTypes = true
}

android {
    namespace = "com.ramitsuri.choresclient.android"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    defaultConfig {
        applicationId = "com.ramitsuri.choresclient.android"
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        versionCode = 49
        versionName = "6.4"

        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=all-compatibility"
    }
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
