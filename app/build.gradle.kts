import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.legacy.kapt)
    id("com.google.gms.google-services")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { inputStream ->
        localProperties.load(inputStream)
    }
}

val adzunaAppId =
    localProperties.getProperty("ADZUNA_APP_ID", "")

val adzunaAppKey =
    localProperties.getProperty("ADZUNA_APP_KEY", "")

android {
    namespace = "com.example.job_connect"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.job_connect"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "ADZUNA_APP_ID",
            "\"$adzunaAppId\""
        )

        buildConfigField(
            "String",
            "ADZUNA_APP_KEY",
            "\"$adzunaAppKey\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // WorkManager background job alerts
    implementation(libs.androidx.work.runtime.ktx)
    androidTestImplementation(libs.androidx.work.testing)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    // Room local database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    testImplementation(libs.androidx.room.testing)

    implementation(
        "androidx.recyclerview:recyclerview:1.4.0"
    )

    // Firebase
    implementation(
        platform("com.google.firebase:firebase-bom:34.19.0")
    )
    implementation(
        "com.google.firebase:firebase-auth"
    )
    implementation(
        "com.google.firebase:firebase-firestore"
    )

    // Retrofit REST API
    implementation(
        "com.squareup.retrofit2:retrofit:3.0.0"
    )
    implementation(
        "com.squareup.retrofit2:converter-gson:3.0.0"
    )

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(
        libs.androidx.espresso.core
    )
}