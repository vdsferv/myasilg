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
        // [추가!] MPAndroidChart 라이브러리가 있는 JitPack 저장소
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Mysilgurae"
include(":app")
