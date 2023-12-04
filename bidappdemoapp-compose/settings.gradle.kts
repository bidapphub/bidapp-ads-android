pluginManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
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
        mavenLocal()
        maven {
            url = uri("https://cboost.jfrog.io/artifactory/chartboost-ads/")
        }
        mavenCentral()
    }
}

rootProject.name = "bidappdemoapp-compose"
include(":app")
 