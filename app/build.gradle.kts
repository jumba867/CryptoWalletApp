plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.ethereumwallet"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ethereumwallet"
        minSdk = 24
        targetSdk = 34
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cronet.embedded)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Web3j for Ethereum
    implementation("org.web3j:core:5.0.0")

    // ZXing for QR Code Scanning
    implementation("com.journeyapps:zxing-android-embedded:4.2.0")

    // Replace SpongyCastle with BouncyCastle
    implementation("org.bouncycastle:bcprov-jdk15on:1.70") // Use BouncyCastle instead
}
