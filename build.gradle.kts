plugins {
	// Version comes from settings.gradle.kts. This picks net.fabricmc.fabric-loom-remap
	// for obfuscated Minecraft (<26) and net.fabricmc.fabric-loom for unobfuscated (>=26).
	id("dev.kikugie.loom-back-compat")
}

// This is Stonecutter's central script: it runs once per version node in versions/.
val minecraftVersion: String = sc.current.version

// 1.21.11 ships Java 21 class files, 26.1+ ship Java 25.
// This single value drives options.release and the ${java} placeholder in both json
// resources, so the bytecode level and the declared compatibility level cannot drift.
val requiredJava: Int = when {
	sc.current.parsed >= "26.1" -> 25
	sc.current.parsed >= "1.20.5" -> 21
	else -> 17
}

// Resolved up front: inside task configuration blocks, `property(...)` would bind to
// Task.property(...) instead of the project's, since Task also declares that method.
val modVersion: String = "${property("mod_version")}+$minecraftVersion"
val loaderVersion: String = property("loader_version") as String

version = modVersion
group = property("maven_group") as String

// Without this a node's artifact is named after its directory, e.g. "26.1.2-1.2.jar".
base {
	archivesName.set("carpet-szf")
}

repositories {
	maven {
		name = "masa"
		url = uri("https://masa.dy.fi/maven")
	}
	maven {
		name = "Modrinth"
		url = uri("https://api.modrinth.com/maven")
	}
}

dependencies {
	// A no-op on unobfuscated versions; on 1.21.11 it is equivalent to
	// mappings(loom.officialMojangMappings()). Using Mojang's official names keeps the
	// shared source identical across versions instead of matching Yarn on 1.21.11.
	loomx.applyMojangMappings()

	minecraft("com.mojang:minecraft:$minecraftVersion")

	// modImplementation stays valid on every node: on unobfuscated Loom
	// loom-back-compat aliases it back to implementation.
	modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
	// Full coordinate rather than a bare version: Carpet's masa.dy.fi maven does not
	// carry every release, so some nodes pull the same build from Modrinth instead.
	modImplementation(property("carpet_dep") as String)
}

java {
	withSourcesJar()
	toolchain {
		// One JDK 25 toolchain drives every node; the bytecode level is set per node
		// by options.release below.
		languageVersion.set(JavaLanguageVersion.of(25))
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(requiredJava)
	options.encoding = "UTF-8"
}

tasks.processResources {
	inputs.property("version", modVersion)
	inputs.property("java", requiredJava)
	inputs.property("minecraft", minecraftVersion)

	// Plain JSON cannot hold Stonecutter comments, so these two files use ${}
	// placeholders filled in at build time instead.
	filesMatching(listOf("fabric.mod.json", "carpet-szf.mixins.json")) {
		expand(
			mapOf(
				"version" to modVersion,
				"java" to requiredJava.toString(),
				"minecraft" to minecraftVersion,
				"loader" to loaderVersion,
			)
		)
	}
}

tasks.jar {
	// The script runs with projectDir == versions/<node>/, so repository-relative
	// paths have to go through rootProject.
	from(rootProject.file("LICENSE")) {
		rename { "${it}_carpet-szf" }
	}
}

// Stonecutter 0.9 has no built-in buildAndCollect (the chiseled tasks were removed in
// 0.7), so register one. loomx.modJar is remapJar on obfuscated nodes and jar on the rest.
// Sync rather than Copy so each per-version directory mirrors the build output exactly;
// a plain copy would leave stale jars behind when an artifact stops being produced.
tasks.register<Sync>("buildAndCollect") {
	group = "build"
	description = "Builds the mod and collects the jars into build/libs/<minecraft version>/"
	from(loomx.modJar)
	from(loomx.modSourcesJar)
	into(rootProject.layout.buildDirectory.dir("libs/$minecraftVersion"))
}
