# Gradle Configuration

The dependencies required for BankTemplate can be configured in [libs. versions. toml](../gradle/libs.versions.toml)

## SDK Version

in  [libs. versions. toml](../gradle/libs.versions.toml) , modify the SDKs version，they will be used for every module.

```kotlin
[versions]
template = 'R3.12'
compileSdk = "34"
minSdk = "25"
targetSdk = "31"
```

In the modules, they are used in this way:

```kotlin
...
android {
      compileSdk = libs.versions.compileSdk.get().toInt()
      defaultConfig {
            minSdk = libs.versions.minSdk.get().toInt()
            targetSdk = libs.versions.targetSdk.get().toInt()
      }
    ...
}
```



## Libraries Dependencies 

### Maven libraries

If you want to modify the maven libraries, you just only modify as follow in  [libs. versions. toml](../gradle/libs.versions.toml) , they will be used for every module.

```kotlin
[versions]
...
appcompat = "1.7.0"
constraintlayout = "2.2.0"
...
[libraries]
...
appcompat = { module = "androidx.appcompat:appcompat", version.ref = "appcompat" }
constraintlayout = { module = "androidx.constraintlayout:constraintlayout", version.ref = "constraintlayout" }
...
```

In the modules, they are used in this way:

```groovy
...
dependencies {
  implementation(libs.appcompat)
    implementation(libs.constraintlayout)
}
```



### Local AAR or JAR

Because [app/build.gradle.kts](../app/build.gradle.kts) will import all aar and jar files in [app/libs](../app/libs), you just only  replace/add/remove the files in the libs.

```groovy
..
dependencies {
    implementation(fileTree("libs") {
        include("*.jar", "*.aar")
    })
    ...
}
...

```

Then, you can import them in other modules:

```groovy
...
dependencies {
    compileOnly(fileTree("../app/libs") {
        include("*.jar", "*.aar")
    })
}
...
```



### Newland Maven Plugins

Newland provides its own Maven libraries.  If you need to use it, please configure it in the following way:

[settings.gradle.kts](../settings.gradle.kts)

```kotlin
pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.newlandnpt.com/repository/maven_hipos/")

            credentials {
                username = "npt.hipos"
                password = "bidpo4-dymgoQ-puhfod"
            }
        }
        ...
    }
}
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.newlandnpt.com/repository/maven_hipos/")

            credentials {
                username = "npt.hipos"
                password = "bidpo4-dymgoQ-puhfod"
            }
        }
       ...
    }
}
```

  

## Customized Generated APK Name

In [app/build.gradle.kts](../app/build.gradle.kts), you can customized the apk name.

```groovy
...
android {
    ...
    android.applicationVariants.all {
        //debug/release
        val time = SimpleDateFormat("yyyyMMddHHmm").format(Date())
        //apk name, such as NSDK-BANKTEMPLATE-R3-1-npi-an-3.6-alpha10-debug-202209060848
        var apkName = rootProject.name +"-" + versionCode + "-" + versionName + "-" + buildType.name + "-" + time
        if (productFlavors != null && productFlavors.size > 0) {
            //Add flavor name if use product flavors
            apkName = apkName + "-" + productFlavors[0].name
        }
        val fileName = apkName.uppercase() + ".apk"
        outputs.all {
            if (this is ApkVariantOutputImpl){
                outputFileName = fileName
            }
        }
    }
}
...
def static releaseTime() {
    return new Date().format("YYYYMMddHHmm")
}
```



## Signing Configs

[newland_debug.keystore](../newland_debug.keystore) is the default signature certificate of the apk. You can replace it with yours.

It's used in [app/build.gradle.kts](../app/build.gradle.kts).

```groovy
...
android {
    ...
    //newland signing configs
     signingConfigs {
        create("newland") {
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storeFile = rootProject.file("newland_debug.keystore")
            storePassword = "android"
        
    }
    buildTypes {
        release {
            //signature file
            signingConfig = signingConfigs.getByName("newland")
        }
        debug {
            //signature file
            signingConfig = signingConfigs.getByName("newland")
        }
    }
    ...
}
```



## Namespace

Android Studio requires replacing the element `package of AndroidManifest.xml` with `namespace` in every module. It will be used as the default prefix package name for `AndroidManifest.xml.`

Defined as follows:

```groovy
...
android {
    namespace 'acquire.app'
    ...
}

```

