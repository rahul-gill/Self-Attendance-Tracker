import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
    private const val versionPatch: Int = 1
    private const val versionBuild: Int = 0
    const val compileSdk: Int = 36
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
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    //basics
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.startup:startup-runtime:1.2.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.12.2")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation("androidx.test.ext:junit-ktx:1.2.1")
    testImplementation("androidx.work:work-testing:2.10.1")

    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")

    //sqlDelight
    implementation("app.cash.sqldelight:android-driver:2.1.0")
    implementation("app.cash.sqldelight:coroutines-extensions:2.1.0")
    implementation("app.cash.sqldelight:primitive-adapters:2.1.0")
    testImplementation("app.cash.sqldelight:sqlite-driver:2.1.0")
    //sugar and water
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_nio:2.1.5")
    //ui thing
    implementation (platform("androidx.compose:compose-bom:2025.10.01"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.10.01"))
    implementation ("androidx.compose.material3:material3")
    // https://mvnrepository.com/artifact/androidx.compose.material/material-icons-extended
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("com.materialkolor:material-kolor:4.0.2")
    implementation("com.github.skydoves:colorpicker-compose:1.1.2")
    implementation("dev.olshevski.navigation:reimagined:1.5.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    androidTestImplementation("tools.fastlane:screengrab:2.1.1")
    //WorkManager for daily alarm scheduling
    implementation("androidx.work:work-runtime-ktx:2.10.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

}
