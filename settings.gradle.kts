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

rootProject.name = "HandHopHop"
include(":app")

// Include the features
include(":feature:mash")
include(":feature:feed")
include(":feature:bookmark")
include(":feature:profile")

// Include the core modules
include(":core:network")
include(":core:session")
include(":core:design")
include(":core:system")
