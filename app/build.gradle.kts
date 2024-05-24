plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
    id("app.cash.sqldelight")
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.github.rahul_gill.attendance")
        }
    }
}

android {
    namespace = "com.github.rahul_gill.attendance"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.github.rahul_gill.attendance"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.1.0"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    sourceSets {
        map { it.java.srcDir("src/${it.name}/kotlin") }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                file("proguard-rules.pro")
            )
            if(!System.getenv("SIGN_RELEASE_WITH_DEBUG_KEY_CI").isNullOrBlank()){
                applicationIdSuffix = ".ci"
                signingConfig = signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        targetCompatibility = JavaVersion.VERSION_17
        sourceCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures{
        viewBinding = true
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12"
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    //basics
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.startup:startup-runtime:1.1.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //sqlDelight
    implementation("app.cash.sqldelight:android-driver:2.0.2")
    implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")
    implementation("app.cash.sqldelight:primitive-adapters:2.0.2")
    testImplementation("app.cash.sqldelight:sqlite-driver:2.0.2")
    //sugar and water
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_nio:2.0.4")
    //ui thing
    implementation (platform("androidx.compose:compose-bom:2024.05.00"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation ("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("com.materialkolor:material-kolor:1.6.0")
    implementation("com.github.skydoves:colorpicker-compose:1.0.7")
    implementation("dev.olshevski.navigation:reimagined:1.5.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    androidTestImplementation("tools.fastlane:screengrab:2.1.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

}
