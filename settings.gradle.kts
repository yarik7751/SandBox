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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SandBox"
include(":app")
include(":utils")
include(":core:navigation")
include(":core:ui")

include(":feature:start:api")
include(":feature:start:impl")

include(":feature:home:api")
include(":feature:home:impl")

include(":feature:websocket:api")
include(":feature:websocket:impl")
