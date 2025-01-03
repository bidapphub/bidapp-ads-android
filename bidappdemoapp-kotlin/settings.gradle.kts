pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea") }
        maven { url = uri("https://android-sdk.is.com/") }
        maven {
            url = uri("https://cboost.jfrog.io/artifactory/chartboost-ads/")
        }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven { url = uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea") }
        maven { url = uri("https://android-sdk.is.com/") }
        maven {
            url = uri("https://cboost.jfrog.io/artifactory/chartboost-ads/")
        }
        mavenCentral()
    }
}

rootProject.name = "bidappdemoappkotlin"
include(":app")
 