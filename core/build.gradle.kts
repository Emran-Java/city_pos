plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "acquire.core"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
//        javaCompileOptions {
//            annotationProcessorOptions {
//                arguments += mapOf("eventBusIndex" to project.getName())
//            }
//        }
        buildConfigField("long", "RELEASE_TIMESTAMP", System.currentTimeMillis().toString())
        buildConfigField("String", "TEMPLATE_VERSION",  "\"${libs.versions.template.get()}\"")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        aidl = true
    }

}

dependencies {
    implementation (project(":database"))
    api (project(":base"))
    api (project(":sdk_helper"))

    implementation(libs.recyclerview)
    implementation(libs.constraintlayout)
    implementation(libs.viewpager2)
    implementation(libs.appcompat)
    implementation(libs.camerax.camear2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.gson)
    implementation(libs.hipay.connect){
        exclude(group ="com.newlandnpt.nexo.scap.sdklibs", module= "nsdk")
        exclude(group ="com.newlandnpt.nexo.scap.sdklibs", module= "emv")
        exclude(group ="com.newlandnpt.nexo.scap.sdklibs", module= "card-emulation")
        exclude(group ="com.newlandnpt.nexo.scap.sdklibs", module= "wireless-dock")
    }
    annotationProcessor(libs.auto.service)

    api(libs.xstream) {
        exclude("xmlpull")
    }

    implementation("com.google.android.material:material:1.11.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")

//    implementation greenrobot['eventbus']
//    annotationProcessor greenrobot['eventbus-compiler']
}