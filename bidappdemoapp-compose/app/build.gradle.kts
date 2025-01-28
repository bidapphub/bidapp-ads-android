plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.bidapp.demo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bidapp.demo"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }


}


dependencies {
    implementation ("io.bidapp:sdk:2.4.0")
    implementation ("io.bidapp.networks:applovin:2.2.5")
    implementation ("io.bidapp.networks:applovinmax:2.2.5")
    implementation ("io.bidapp.networks:unity:2.3.0")
    implementation ("io.bidapp.networks:liftoff:2.3.0")
    implementation ("io.bidapp.networks:admob:2.2.5")
    implementation ("io.bidapp.networks:chartboost:2.2.5")
    implementation ("io.bidapp.networks:digitalturbine:2.3.5")
    implementation ("io.bidapp.networks:facebook:2.2.5")
    implementation ("io.bidapp.networks:startIo:2.2.5")
    implementation ("io.bidapp.networks:yandex:2.2.5")
    implementation ("io.bidapp.networks:mytarget:2.2.5")
    implementation ("io.bidapp.networks:inmobi:2.3.0")
    implementation ("io.bidapp.networks:bigoads:2.2.5")
    implementation ("io.bidapp.networks:ironsource:2.3.5")
    implementation ("io.bidapp.networks:mintegral:2.3.5")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}