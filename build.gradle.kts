
plugins {
  alias(libs.plugins.kotlin)
  id("java-library")
  id("maven-publish")
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

java {
  withSourcesJar()
}

kotlin {
  jvmToolchain(providers.gradleProperty("jvmVersion").get().toInt())
}

repositories {
  mavenCentral()
  mavenLocal()
}

dependencies {
  api(libs.gson)
}

publishing {
  publications {
    create<MavenPublication>("myLibrary") {
      from(components["java"])
    }
  }

  repositories {
    mavenLocal()
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/doki-theme/doki-build-source-jvm")
      credentials {
        username = System.getenv("GITHUB_ACTOR")
        password = System.getenv("GITHUB_TOKEN")
      }
    }
  }
}
