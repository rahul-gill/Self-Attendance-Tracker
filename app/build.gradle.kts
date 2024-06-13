plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
    id("app.cash.sqldelight")
    id("org.jetbrains.kotlin.plugin.compose")
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.github.rahul_gill.attendance")
        }
    }
}

object VersionProperties {
    private const val versionMajor: Int = 1
    private const val versionMinor: Int = 0
    private const val versionPatch: Int = 0
    private const val versionBuild: Int = 5
    const val compileSdk: Int = 34
    const val minSdk: Int = 26

    val versionCode
        get() = versionMajor * 10000 + versionMinor * 1000 + versionPatch * 100 + versionBuild
    val versionName
        get() = "$versionMajor.$versionMinor.$versionPatch"
}

android {
    namespace = "com.github.rahul_gill.attendance"
    compileSdk = VersionProperties.compileSdk

    defaultConfig {
        applicationId = "com.github.rahul_gill.attendance"
        minSdk = VersionProperties.minSdk
        targetSdk = VersionProperties.compileSdk
        versionCode = VersionProperties.versionCode
        versionName = VersionProperties.versionName
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
            ndk.debugSymbolLevel = "FULL"
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
        buildConfig = true
        compose = true
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
    implementation (platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation ("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("com.materialkolor:material-kolor:1.6.1")
    implementation("com.github.skydoves:colorpicker-compose:1.0.7")
    implementation("dev.olshevski.navigation:reimagined:1.5.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    androidTestImplementation("tools.fastlane:screengrab:2.1.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

}
