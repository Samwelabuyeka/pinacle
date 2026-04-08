pluginManagement {
    repositories {
        google()
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

rootProject.name = "pinacle-native"
include(":app")

val mlc4jDir = file("dist/lib/mlc4j")
if (mlc4jDir.exists()) {
    include(":mlc4j")
    project(":mlc4j").projectDir = mlc4jDir
}
