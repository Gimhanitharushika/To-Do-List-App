// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // You might already have this, but add if missing:
        classpath("com.android.tools.build:gradle:8.0.2")  // Replace with your current AGP version

        // Add the Google Services plugin classpath for Firebase
        classpath("com.google.gms:google-services:4.3.15")
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

