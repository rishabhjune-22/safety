plugins {

    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.safety"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.safety"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.gridlayout)
    implementation(libs.play.services.location)
    implementation(libs.core)
    implementation(libs.espresso.core)
    implementation(libs.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation ("com.github.yalantis:ucrop:2.2.6")
    implementation("com.github.dhaval2404:imagepicker:2.1")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation (libs.firebase.storage)
    implementation ("androidx.activity:activity:1.7.0")
    implementation("com.vanniktech:android-image-cropper:4.6.0")
    implementation("com.google.firebase:firebase-storage")
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation ("com.google.android.material:material")
    implementation("com.github.skydoves:balloon:1.6.9")
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.google.android.material:material:1.9.0") // Use latest version

    implementation ("org.osmdroid:osmdroid-android:6.1.6")
    implementation ("org.osmdroid:osmdroid-wms:6.1.6")
    implementation ("org.osmdroid:osmdroid-mapsforge:6.1.6")
    implementation ("org.osmdroid:osmdroid-geopackage:6.1.6")

}