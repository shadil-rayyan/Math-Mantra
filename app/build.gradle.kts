plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.zendalona.zmantra"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zendalona.zmantra"
        minSdk = 26
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
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation(libs.lottie)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.exp4j)

    // Testing Dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1") // Optional
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // Android Instrumentation Tests
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("org.mockito:mockito-android:4.11.0")
    debugImplementation("androidx.fragment:fragment-testing:1.5.7")

    // Espresso UI Testing
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1") // Espresso core
    androidTestImplementation("androidx.test.espresso:espresso-idling-resource:3.5.1") // Optional for handling idle resources

    // JUnit4 Support for running tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5") // JUnit4 for Android

    // Test Runner for Android tests
    androidTestImplementation("androidx.test:runner:1.5.2") // Test runner for Android tests

    // Fragment Testing APIs
    debugImplementation("androidx.fragment:fragment-testing:1.5.5") // For fragment-related testing

    // To launch fragments in test
    debugImplementation("androidx.fragment:fragment-testing:1.5.5") // Fragment testing APIs

    // Lifecycle support for testing lifecycle-related aspects of fragments
    androidTestImplementation("androidx.lifecycle:lifecycle-runtime-testing:2.5.1") // For lifecycle testing

    // Core Test library for Android
    androidTestImplementation("androidx.test:core:1.5.0") // Core functionality for AndroidX testing


    // Optional for mocking the navigation component (if you use NavController or a similar component)
    androidTestImplementation("androidx.navigation:navigation-testing:2.5.3") // Navigation testing support
}
