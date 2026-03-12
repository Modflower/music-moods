import moe.amp.AutoMixin.autoMixinFabric
import net.fabricmc.loom.task.RemapJarTask

plugins {
	alias(libs.plugins.universal)
	alias(libs.plugins.machete)
	id("versions")
	id("mod-publish")
}

val minecraft: String by project
val minecraftRequired: String by project

val modrinthId: String by project
val projectVersion: String by project

loom {
	runConfigs {
		"client" {
			ideConfigGenerated(true)
		}
		"server" {
			ideConfigGenerated(false)
		}
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
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

		filesMatching(listOf("fabric.mod.json")) { expand(map) }

		exclude(
			"*.mixin.json",
			"*.mixins.json",
			"META-INF/mods.toml",
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
	jar {
		finalizedBy("optimizeOutputsOfJar")
	}
	"modrinth" {
		dependsOn("optimizeOutputsOfJar")
	}
}

modrinth {
	uploadFile.set(tasks.jar.get())
}
