plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

val googleMapsApiKey = project.findProperty("GOOGLE_MAPS_API_KEY") as? String ?: ""
//val firebaseApiKey = project.findProperty("FIREBASE_API_KEY") as? String ?: ""
android {
    buildFeatures {
        buildConfig = true
    }
    namespace = "com.example.lahjaily_monumentquiz"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.lahjaily_monumentquiz"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"${googleMapsApiKey}\"")
//        buildConfigField("String", "FIREBASE_API_KEY", "\"${firebaseApiKey}\"")
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
    implementation(libs.firebase.auth)
    implementation(libs.play.services.location)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.appcompat:appcompat:1.6.1")
}