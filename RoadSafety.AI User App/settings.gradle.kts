import java.net.URI

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
        maven { url=URI( "https://storage.zego.im/maven") }
        maven { url=URI("https://jitpack.io") }
    }
}

rootProject.name = "My Application"
include(":app")
