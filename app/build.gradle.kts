plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.krishna.soslocation"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.krishna.soslocation"
        minSdk = 24
        targetSdk = 36
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.8.0")

    // Google Play Services
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

//    MAP
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.maps.android:maps-ktx:5.0.0")

}
//--------------------------------------------------------------------------------------------------------------------------------
//
//plugins {
//    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
//}
//
//android {
//    namespace = "com.krishna.soslocation"
//    compileSdk = 36
//
//    defaultConfig {
//        applicationId = "com.krishna.soslocation"
//        minSdk = 24
//        targetSdk = 36
//        versionCode = 1
//        versionName = "1.0"
//
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_11
//        targetCompatibility = JavaVersion.VERSION_11
//    }
//    kotlinOptions {
//        jvmTarget = "11"
//    }
//}
//
//dependencies {
//
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    implementation(libs.androidx.activity)
//    implementation(libs.androidx.constraintlayout)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//
//    implementation("com.google.android.gms:play-services-location:21.0.1")
//    implementation("com.google.android.gms:play-services-maps:18.2.0")
//    implementation("com.google.android.material:material:1.11.0")
//    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
//
//    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
//    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
//
//    // Activity and Fragment KTX
//    implementation ("androidx.activity:activity-ktx:1.8.2")
//    implementation ("androidx.fragment:fragment-ktx:1.6.2")
//
//    // Testing dependencies
//    testImplementation ("junit:junit:4.13.2")
//    androidTestImplementation ("androidx.test.ext:junit:1.1.5'")
//    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
//
//    // Firebase
//
//    implementation (platform("com.google.firebase:firebase-bom:33.4.0"))
//    implementation ("com.google.firebase:firebase-auth-ktx")
//    implementation ("com.google.firebase:firebase-firestore-ktx")
//    implementation ("com.google.firebase:firebase-analytics-ktx")
//}