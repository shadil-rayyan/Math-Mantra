plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.dagger.hilt.android") // Add this
    kotlin("kapt")
}

android {
    namespace = "com.zendalona.zmantra"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zendalona.zmantra"
        minSdk = 30
        targetSdk = 35
        versionCode = 3
        versionName = "1.0.9"
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
    // Core
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.fragment)
    implementation(libs.androidx.preference.ktx)
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-compiler:2.52")

    // Third-party
    implementation(libs.poi)
    implementation(libs.poi.ooxml)
    implementation(libs.lottie)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    implementation(libs.exp4j)

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.core.testing)

    // Android Instrumentation Tests
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.idling.resource)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.lifecycle.runtime.testing)
    androidTestImplementation(libs.navigation.testing)

    // Fragment testing (debug only)
    debugImplementation(libs.fragment.testing)

    // Leak detection
    debugImplementation(libs.leakcanary)
    testImplementation(kotlin("test"))
}
