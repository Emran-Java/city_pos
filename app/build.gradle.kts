import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import java.text.SimpleDateFormat
import java.util.Date


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.zztl.pos.city"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.zztl.pos.city"

        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.00.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    //newland signing configs
    signingConfigs {
        create("newland") {
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storeFile = rootProject.file("newland_debug.keystore")
            storePassword = "android"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            //proguard file
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            //signature file
            signingConfig = signingConfigs.getByName("newland")
        }
        debug {
           /* isMinifyEnabled = true
            isShrinkResources = true
            //proguard file
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )*/
            //signature file
            signingConfig = signingConfigs.getByName("newland")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    android.applicationVariants.all {
        //debug/release
        val time = SimpleDateFormat("yyyyMMddHHmm").format(Date())
        //apk name, such as NSDK_BANKTEMPLATE-3120-3.12.0-RELEASE-202503271117.apk
        var apkName =
            rootProject.name + "-" + versionCode + "-" + versionName + "-" + buildType.name + "-" + time
        if (productFlavors != null && productFlavors.size > 0) {
            //Add flavor name if use product flavors
            apkName = apkName + "-" + productFlavors[0].name
        }
        val fileName = apkName.uppercase() + ".apk"
        outputs.all {
            if (this is ApkVariantOutputImpl) {
                outputFileName = fileName
            }
        }
    }
}

dependencies {
    implementation(fileTree("libs") {
        include("*.jar", "*.aar")
    })
    configurations.all {
        exclude( group = "xmlpull", module = "xmlpull")
    }

    implementation(libs.gson)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.viewpager2)
    implementation(libs.core.ktx)
    implementation(libs.activity)
//    debugImplementation (libs.leakcanary)
    testImplementation(libs.junit)

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.runner)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // --- navigation ---
    //def nav_version "2.5.2"
    implementation ("androidx.navigation:navigation-fragment-ktx:2.5.2")
    implementation ("androidx.navigation:navigation-ui-ktx:2.5.2")
    // Feature module Support
    implementation ("androidx.navigation:navigation-dynamic-features-fragment:2.5.2")
    // ----------------------------------------------------------------------------------------


    implementation(project(":base"))
    implementation(project(":core"))
    implementation(project(":database"))
    implementation(project(":settings"))

}



