plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
//    id("com.google.dagger.hilt.android")
    id("app.cash.sqldelight")
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.github.rahul_gill.attendance")
            verifyMigrations.set(true)
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
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables.useSupportLibrary = true

    }
    sourceSets {
        map { it.java.srcDir("src/${it.name}/kotlin") }
    }
    signingConfigs {
        getByName("debug") {
            storeFile = file("$projectDir/keystore.jks")
            storePassword = "password"
            keyAlias = "key0"
            keyPassword = "password"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                file("proguard-rules.pro")
            )
            signingConfig = signingConfigs.getByName("debug")
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
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.fragment:fragment-ktx:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("androidx.preference:preference-ktx:1.2.0")

    implementation("androidx.datastore:datastore-preferences:1.1.0")
    implementation("androidx.startup:startup-runtime:1.1.1")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    //navigation
    val nav_version = "2.5.3"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    //sqldelight
    implementation("app.cash.sqldelight:android-driver:2.0.0-alpha05")
    implementation("app.cash.sqldelight:coroutines-extensions:2.0.0-alpha05")
    implementation("app.cash.sqldelight:primitive-adapters:2.0.0-alpha05")


    implementation("com.russhwolf:multiplatform-settings-no-arg:1.0.0")
    implementation("com.russhwolf:multiplatform-settings-coroutines:1.0.0")


    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_nio:2.0.3")




    implementation (platform("androidx.compose:compose-bom:2024.04.01"))
    implementation ("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("com.materialkolor:material-kolor:1.3.0")
    implementation("com.github.skydoves:colorpicker-compose:1.0.7")
    implementation("dev.olshevski.navigation:reimagined:1.5.0")
}
