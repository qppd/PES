plugins {
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // alias(libs.plugins.google.gms.google.services) // Removed for no Firebase
}

android {
    namespace = "com.qppd.pesapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.qppd.pesapp"
        minSdk = 23
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
        compose = true
        viewBinding = true
    }
}

dependencies {
    // Supabase dependencies
    implementation(platform("io.github.jan-tennert.supabase:bom:2.3.3"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.ktor:ktor-client-android:2.3.9")

    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.datastore:datastore-preferences-core:1.0.0")
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.compose.material:material:1.5.4")
    // implementation(libs.firebase.database) // Removed for no Firebase
    // implementation(libs.firebase.auth)
    // implementation(libs.firebase.firestore)
    // implementation(libs.firebase.storage)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}