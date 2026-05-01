plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // KSP -> Room's annotation processor lives here. Way faster than kapt.
    alias(libs.plugins.ksp)
}

android {
    namespace = "ca.gbc.comp3074.scavengerhunt"
    compileSdk = 36

    defaultConfig {
        applicationId = "ca.gbc.comp3074.scavengerhunt"
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

    // Turning on view binding so I don't have to keep typing findViewById everywhere.
    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // --- Android Studio defaults (came with the new project) ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // --- Stuff I had to add to make the app actually work ---

    // Room: local SQLite database. ksp() is for the compile-time codegen,
    // implementation() is for what runs in the app at runtime.
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // ViewModel / LiveData. Survives rotation, keeps DB queries off the UI thread.
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // RecyclerView for the hunt list, CardView for the row backgrounds
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)

    // --- Tests (didn't really write any, but leaving the wiring in place) ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
