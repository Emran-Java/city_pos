pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://maven.newlandnpt.com/repository/maven_hipos/")
            credentials {
                username = "npt_common"
                password = "L9n3OeQ6r7SXdYvOOgLyUnrQXd206BIz"
            }
        }
        maven {
            url = uri("https://maven.newlandnpt.com/repository/maven-npt-common/")
            credentials {
                username = "npt_common"
                password = "L9n3OeQ6r7SXdYvOOgLyUnrQXd206BIz"
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://maven.newlandnpt.com/repository/maven_hipos/")

            credentials {
                username = "npt_common"
                password = "L9n3OeQ6r7SXdYvOOgLyUnrQXd206BIz"
            }
        }
        maven {
            url = uri("https://maven.newlandnpt.com/repository/maven-npt-common/")
            credentials {
                username = "npt_common"
                password = "L9n3OeQ6r7SXdYvOOgLyUnrQXd206BIz"
            }
        }

    }
}

rootProject.name = "city_pos"
include(":base")
include(":app")
include(":database")
include(":core")
include(":settings")
include(":sdk_helper")
