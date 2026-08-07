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
    namespace 'com.your.package.name'
    compileSdk 34  // <-- Update this to 34 or higher

    defaultConfig {
        applicationId "com.your.package.name"
        minSdk 21     // <-- Sometimes a dependency requires you to raise this too
        targetSdk 34  // <-- Update this to match
        versionCode 1
        versionName "1.0"
    }
}
