import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
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
			memoryMaximumSize = "4G"
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
		enabled = false
	}
	remapJar {
		destinationDirectory.value(jar.get().destinationDirectory)
	}
	register<ShadowJar>("shadowRemapJar") {
		dependsOn(remapJar)
		from(zipTree(remapJar.get().archiveFile.get()))
		archiveClassifier = null
		configurations.value(setOf(project.configurations.shadow.get()))
		relocate("com.llamalad7.mixinextras", "gay.ampflower.musicmoods.mixinextras")
		relocate("com.bawnorton.mixinsquared", "gay.ampflower.musicmoods.mixinsquared")
		manifest.attributes("MixinConfigs" to "music-moods.mixin.json")
		mergeServiceFiles()
		finalizedBy("optimizeOutputsOfShadowRemapJar")
	}
	"modrinth" {
		dependsOn("optimizeOutputsOfShadowRemapJar")
	}
}

modrinth {
	uploadFile.set(tasks.named("shadowRemapJar").get())
}

machete {
	additionalTasks.add("shadowRemapJar")
	ignoredTasks.addAll("jar", "remapJar", "shadowJar")
}
