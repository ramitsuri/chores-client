import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlin.parcelize)
}

version = "1.0"

kotlin {
    jvmToolchain(11)
    android {
        namespace = "com.ramitsuri.choresclient"
        compileSdk = 37
        minSdk = 26

        withJava()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Network
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.auth)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Date Time
            implementation(libs.kotlinx.datetime)

            // Koin
            implementation(libs.koin.core)

            // Logging
            implementation(libs.kermit)

            // Serialization
            implementation(libs.kotlinx.serialization.core)

            // Settings
            api(libs.multiplatform.settings)

            // SQL
            implementation(libs.sqldelight.coroutines.extensions)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test.common)
            implementation(libs.kotlin.test.annotations.common)
            implementation(libs.ktor.client.mock)
            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            // Encrypted SharedPrefs
            implementation(libs.androidx.security.crypto)

            // SQL
            implementation(libs.sqldelight.android.driver)

            // Network
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.android)

            // ViewModel
            implementation(libs.androidx.lifecycle.viewmodel.ktx)

            // Firebase
            implementation(project.dependencies.enforcedPlatform(libs.firebase.bom.get()))
            implementation(libs.firebase.database.ktx)
            implementation(libs.firebase.messaging.ktx)
        }
        androidUnitTest.dependencies {
            implementation(libs.kotlin.test.junit)
            implementation(libs.junit)
        }
    }
}

sqldelight {
    databases {
        create("ChoresDatabase") {
            packageName.set("com.ramitsuri.choresclient.db")
        }
    }
}
