pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // Обязательно: репозиторий для KSP
        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-ksp/maven")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Тоже добавляем сюда, если будут зависимости из этого репозитория
        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-ksp/maven")
    }
}

rootProject.name = "Your Bar"
include(":app")
