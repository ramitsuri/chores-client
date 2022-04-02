import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("com.squareup.sqldelight")
    id ("kotlin-parcelize")
}

version = "1.0"

kotlin {
    android()

    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget = when {
        System.getenv("SDK_NAME")?.startsWith("iphoneos") == true -> ::iosArm64
        System.getenv("NATIVE_ARCH")?.startsWith("arm") == true -> ::iosSimulatorArm64
        else -> ::iosX64
    }

    iosTarget("ios") {}

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../ios/Podfile")
        framework {
            baseName = "shared"
        }
    }

    sourceSets {
        val ktor = findProperty("version.ktor")
        val commonMain by getting {
            dependencies {
                // Network
                implementation("io.ktor:ktor-client-core:$ktor")
                implementation("io.ktor:ktor-client-serialization:$ktor")
                implementation("io.ktor:ktor-client-logging:$ktor")

                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${findProperty("version.kotlinx.coroutines")}")

                // Date Time
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")

                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.0")

                // Settings
                api("com.russhwolf:multiplatform-settings:0.8.1")

                // SQL
                implementation("com.squareup.sqldelight:coroutines-extensions:1.5.3")
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
                implementation ("androidx.security:security-crypto:1.1.0-alpha03")

                // SQL
                implementation("com.squareup.sqldelight:android-driver:1.5.3")

                //Network
                implementation("io.ktor:ktor-client-okhttp:${findProperty("version.ktor")}")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }
        val iosMain by getting {
            dependencies {
                // SQL
                implementation("com.squareup.sqldelight:native-driver:1.5.3")

                // Network
                implementation("io.ktor:ktor-client-ios:$ktor")
            }
        }
        val iosTest by getting
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}

sqldelight {
    database("ChoresDatabase") {
        packageName = "com.ramitsuri.choresclient.db"
    }
}