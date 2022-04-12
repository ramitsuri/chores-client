buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${findProperty("version.kotlin")}")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${findProperty("version.kotlin")}")
        classpath("com.android.tools.build:gradle:${findProperty("version.androidGradlePlugin")}")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.41")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.5")
        classpath("com.squareup.sqldelight:gradle-plugin:1.5.3")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}