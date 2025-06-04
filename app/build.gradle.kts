plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.zendalona.mathsmantra"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zendalona.mathsmatra"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx.v1101)
    implementation(libs.androidx.lifecycle.runtime.ktx.v261)
    implementation(libs.androidx.activity)

    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.fragment)

    implementation(libs.lottie)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    implementation(libs.exp4j)

// Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)




}