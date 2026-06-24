plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "acquire.sdk"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
//    api files("../app/libs/TOMSClientApi_V1.0.05.aar")
    compileOnly(fileTree("../app/libs") {
        include("*.jar", "*.aar")
    })
    implementation (project(":base"))
    implementation(libs.appcompat)
    //These libraries for FLY KEY
    implementation(libs.retrofit.gson)
    implementation(libs.retrofit)
    implementation(libs.gson)
}