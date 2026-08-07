// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}
android {
    namespace 'com.yourname.appname'
    compileSdk 34  // <-- CHANGE THIS TO 34 or 35

    defaultConfig {
        applicationId "com.yourname.appname"
        minSdk 24      // <-- Make sure this isn't too low (some libraries require 24+)
        targetSdk 34   // <-- CHANGE THIS TO MATCH compileSdk
        versionCode 1
        versionName "1.0"
    }
}

