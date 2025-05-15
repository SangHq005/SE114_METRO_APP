plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.metro_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.metro_app"
        minSdk = 28
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Library
    implementation("com.google.android.material:material:1.11.0")
    implementation(libs.glide)
    implementation("com.wdullaer:materialdatetimepicker:4.2.3")
    implementation(libs.chip.navigation.bar)
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation(libs.viewpager)

    // Mapview
    implementation("org.osmdroid:osmdroid-android:6.1.16")
    implementation("org.osmdroid:osmdroid-wms:6.1.16")

    // Other dependencies
    implementation("androidx.preference:preference:1.2.1")

    // Firebase BoM and related dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Credential Manager and Google services
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.google.android.gms:play-services-maps:19.0.0")

    // VNPay Mobile SDK dependencies
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(files("libs/merchant-1.0.25.aar"))

    implementation(libs.appcompat) // androidx.appcompat
    implementation(libs.activity) // androidx.activity
    implementation(libs.constraintlayout) // androidx.constraintlayout
}

configurations.all {
    resolutionStrategy {
        force("org.osmdroid:osmdroid-android:6.1.16")
    }
}