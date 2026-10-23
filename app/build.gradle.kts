plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.saeed.musicapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.saeed.musicapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended") // link, music note, mic, album, tune, paste icons
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7") // ViewModel + viewModelScope
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7") // collectAsStateWithLifecycle

    // Import & Convert pipeline
    implementation("com.github.teamnewpipe:NewPipeExtractor:v0.26.5") // step 1: URL -> direct audio stream URL
    implementation("com.squareup.okhttp3:okhttp:4.12.0")              // HTTP client NewPipeExtractor's Downloader needs
    implementation("androidx.media3:media3-transformer:1.11.0")       // step 3: raw audio -> AAC
    implementation("androidx.media3:media3-common:1.11.0")

    // Library storage (song metadata)
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")   // suspend DAO functions + Flow
    ksp("androidx.room:room-compiler:2.7.2")           // generates the DAO/Database code at build time

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
