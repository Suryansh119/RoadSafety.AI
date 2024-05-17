plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.safezoneadmin"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.safezoneadmin"
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
    buildFeatures{
        viewBinding =true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-firestore:24.9.1")
    implementation("com.google.firebase:firebase-database-ktx:20.3.0")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.airbnb.android:lottie:6.1.0")
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation ("com.github.dhaval2404:imagepicker:2.1")
    implementation ("com.github.ybq:Android-SpinKit:1.4.0")
    implementation("com.otaliastudios:zoomlayout:1.9.0")
    implementation("io.coil-kt:coil:2.5.0")
    implementation ("com.github.Toxa2033:ScaleAndSwipeDismissImageView:v0.7")
    implementation ("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:+")
    implementation ("com.github.skydoves:powermenu:2.2.4")

}