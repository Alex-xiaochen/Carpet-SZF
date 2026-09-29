pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		maven {
			// dev.kikugie.loom-back-compat lives here
			name = "KikuGie Releases"
			url = uri("https://maven.kikugie.dev/releases")
		}
		mavenCentral()
		gradlePluginPortal()
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
	id("dev.kikugie.stonecutter") version "0.9.8"
	id("dev.kikugie.loom-back-compat") version "0.4.3"
}

stonecutter {
	kotlinController = true
	centralScript = "build.gradle.kts"

	create(rootProject) {
		versions("1.21.11", "26.1.2", "26.2", "26.3")
		// The committed state of src/ is written for 26.1.2.
		// The default is the first registered version, so it must be overridden.
		vcsVersion = "26.1.2"
	}
}

// Should match your modid
rootProject.name = "carpet-szf"
