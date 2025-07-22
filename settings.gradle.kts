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
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Warehouse Management"
include(":app")
include(":feature-goods-acceptance")
include(":feature-goods-transfer")
include(":feature-auth")
include(":feature-common")
include(":feature-home")
include(":data-repository")
include(":data-remote")
include(":data-local")
include(":domain")
include(":common")
include(":feature-sync")
