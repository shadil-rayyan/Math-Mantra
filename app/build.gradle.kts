plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.zendalona.zmantra"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zendalona.zmantra"
        minSdk = 30
        targetSdk = 35
        versionCode = 3
        versionName = "1.0.9 "

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }



    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    viewBinding {
        enable = true
    }

}
dependencies {

    // Core Android Libraries
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx.v1101)
    implementation(libs.androidx.lifecycle.runtime.ktx.v261)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.fragment)

    // Third-party Libraries
    implementation(libs.poi)
    implementation(libs.poi.ooxml)
    implementation(libs.lottie)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.exp4j)

    // Testing Dependencies
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test) // Optional
    testImplementation(libs.androidx.core.testing)

    // Android Instrumentation Tests
    androidTestImplementation(libs.androidx.espresso.core.v351)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.mockito.android)
    debugImplementation(libs.androidx.fragment.testing)

    // Espresso UI Testing
    androidTestImplementation(libs.androidx.espresso.core.v351) // Espresso core
    androidTestImplementation(libs.androidx.espresso.idling.resource)

    // JUnit4 Support for running tests
    androidTestImplementation(libs.androidx.junit.v130) // JUnit4 for Android

    // Test Runner for Android tests
    androidTestImplementation(libs.androidx.runner) // Test runner for Android tests

    // Fragment Testing APIs
    debugImplementation(libs.androidx.fragment.testing.v155) // For fragment-related testing

    // To launch fragments in test
    debugImplementation(libs.androidx.fragment.testing) // Fragment testing APIs

    // Lifecycle support for testing lifecycle-related aspects of fragments
    androidTestImplementation(libs.androidx.lifecycle.runtime.testing) // For lifecycle testing

    // Core Test library for Android
    androidTestImplementation(libs.androidx.core) // Core functionality for AndroidX testing


    // Optional for mocking the navigation component (if you use NavController or a similar component)
    androidTestImplementation(libs.androidx.navigation.testing) // Navigation testing support
    debugImplementation (libs.leakcanary.android)

}
