import moe.amp.AutoMixin.autoMixinFabric
import net.fabricmc.loom.task.RemapJarTask

plugins {
	alias(libs.plugins.forge)
	alias(libs.plugins.machete)
	alias(libs.plugins.shadow)
	id("versions")
	id("mod-publish")
}

val minecraftRequired: String by project

val modrinthId: String by project
val projectVersion: String by project

val mappingsAttribute = Attribute.of("net.minecraft.mappings", String::class.java)

loom {
	runConfigs {
		"client" {
			ideConfigGenerated(true)
		}
		"server" {
			ideConfigGenerated(false)
		}
	}
	forge {
		mixinConfig("music-moods.mixin.json")
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
	maven("https://oss.sonatype.org/content/repositories/snapshots") {
		name = "Nexus Repository OSS"
	}
	maven("https://api.modrinth.com/maven") {
		name = "Modrinth"
	}
	maven("https://files.minecraftforge.net/") { name = "Fo" }
	maven("https://maven.gegy.dev") { name = "Gegy" }
	maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
}

dependencies {
	mappings(loom.officialMojangMappings())
}

tasks {
	withType<JavaCompile> {
		dependsOn(":witch:shadowJar")
		options.encoding = "UTF-8"
		options.isDeprecation = true
		options.isWarnings = true
		options.compilerArgs.addAll(
			listOf(
				"-Xplugin:Manifold --no-bootstrap",
				"-implicit:class",
			)
		)
		options.compilerArgs.addAll(manifold.map { (k, v) -> "-A$k=$v" })
		options.isFork = true
		options.forkOptions {
			memoryMaximumSize = "2G"
			// Evil witch has been here...
			// Ignores build.properties so that Manifold doesn't pick up the root properties used by IntelliJ,
			// which when loaded, breaks the build by having impossible conditions.
			jvmArgs!!.add("-javaagent:${project(":witch").layout.buildDirectory.get().asFile.resolve("libs/witch.jar")}")
			jvmArgs!!.add("-XX:+ExitOnOutOfMemoryError")
			jvmArgs!!.add("-XX:+UseParallelGC")
		}

		doLast {
			autoMixinFabric(sourceSets)
		}
	}
	processResources {
		val map =
			mapOf(
				"id" to rootProject.name,
				"java" to java.targetCompatibility.majorVersion,
				"version" to project.version,
				"project_version" to projectVersion,
				"minecraft_required" to minecraftRequired,
				"modrinthId" to modrinthId,
			)
		inputs.properties(map)

		filesMatching(listOf("META-INF/mods.toml")) { expand(map) }

		exclude(
			"*.mixin.json",
			"*.mixins.json",
			"fabric.mod.json",
			"META-INF/neoforge.mods.toml",
		)
	}
	javadoc {
		(options as StandardJavadocDocletOptions).tags("reason:a:Reason")
	}
	withType<Zip> {
		if (this !is RemapJarTask) {
			from(rootProject.file("LICENSE"))
		}
	}
	shadowJar {
		configurations.value(setOf(project.configurations.shadow.get()))
		relocate("com.llamalad7.mixinextras", "gay.ampflower.musicmoods.mixinextras")
		mergeServiceFiles()
	}
	remapJar {
		dependsOn(shadowJar)
		this.inputFile.value(shadowJar.get().archiveFile)
		finalizedBy("optimizeOutputsOfRemapJar")
	}
	"modrinth" {
		dependsOn("optimizeOutputsOfRemapJar")
	}
}

modrinth {
	uploadFile.set(tasks.remapJar.get())
}
