plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("app.cash.sqldelight")
    id("kotlin-parcelize")
}

version = "1.0"

kotlin {
    jvmToolchain(8)
    androidTarget()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "Chores"
        }
    }

    sourceSets {
        val ktor = findProperty("version.ktor")
        val sqlDelight = findProperty("version.sqldelight")
        val commonMain by getting {
            dependencies {
                // Network
                implementation("io.ktor:ktor-client-core:$ktor")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor")
                implementation("io.ktor:ktor-client-logging:$ktor")
                implementation("io.ktor:ktor-client-content-negotiation:$ktor")
                implementation("io.ktor:ktor-client-auth:$ktor")

                // Coroutines
                val coroutines = findProperty("version.kotlinx.coroutines")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")

                // Date Time
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

                // Koin
                val koin = findProperty("version.koin")
                implementation("io.insert-koin:koin-core:$koin")

                // Logging
                val kermitLog = findProperty("version.kermit")
                implementation("co.touchlab:kermit:$kermitLog")

                // Serialization
                val serialization = findProperty("version.kotlinx.serialization")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serialization")

                // Settings
                val settings = findProperty("version.multiplatformSettings")
                api("com.russhwolf:multiplatform-settings:$settings")

                // SQL
                implementation("app.cash.sqldelight:coroutines-extensions:$sqlDelight")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                // Encrypted SharedPrefs
                implementation("androidx.security:security-crypto:1.1.0-alpha03")

                // SQL
                implementation("app.cash.sqldelight:android-driver:$sqlDelight")

                // Network
                implementation("io.ktor:ktor-client-okhttp:$ktor")
                implementation("io.ktor:ktor-client-core:$ktor")
                implementation("io.ktor:ktor-client-android:$ktor")

                // ViewModel
                implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

                // Firebase
                implementation(project.dependencies.platform("com.google.firebase:firebase-bom:30.1.0"))
                implementation("com.google.firebase:firebase-database-ktx")
                implementation("com.google.firebase:firebase-messaging-ktx")
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                // SQL
                implementation("app.cash.sqldelight:native-driver:$sqlDelight")

                // Network
                implementation("io.ktor:ktor-client-ios:$ktor")
            }
        }
    }
}

android {
    namespace = "com.ramitsuri.choresclient"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    buildFeatures {
        buildConfig = true
    }
}

sqldelight {
    databases {
        create("ChoresDatabase") {
            packageName.set("com.ramitsuri.choresclient.db")
        }
    }
}