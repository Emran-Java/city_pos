plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "acquire.base"
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
    api(fileTree("libs") {
        include("*.jar", "*.aar")
    })
    implementation(libs.appcompat)
    implementation(libs.recyclerview)
    implementation(libs.preference)
    implementation(libs.lifecycle.extensions)
    api(libs.material)
    api(libs.lottie)
    api(libs.zxing)
    api(libs.okhttp)
    api(libs.sensitive.guard)
    /*preference viewmodel ktx is 2.3.1, conflict with material&appcompat(viewmodel 2.5.1)*/
//    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1'
}