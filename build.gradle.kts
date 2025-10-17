/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonWriter
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

plugins {
	java
	alias(libs.plugins.loom)
	alias(libs.plugins.minotaur)
	id("versions")
}

buildscript {
	dependencies {
		classpath("com.grack:nanojson:1.10")
		classpath("org.ow2.asm:asm:9.8")
	}
}

val minecraftVersion: String by project
val minecraftRequired: String by project
val minecraftForgeRequired: String by project
val minecraftCompatible: String by project

val modrinthId: String by project
val projectVersion: String by project

val isPublish = System.getenv("GITHUB_EVENT_NAME") == "release"
val isRelease = System.getenv("BUILD_RELEASE").toBoolean()
val isActions = System.getenv("GITHUB_ACTIONS").toBoolean()
val baseVersion: String = "$projectVersion+mc.${minecraftVersion}"

version =
	when {
		isRelease -> baseVersion
		isActions ->
			"$baseVersion-build.${System.getenv("GITHUB_RUN_NUMBER")}-commit.${System.getenv("GITHUB_SHA").substring(0, 7)}-branch.${System.getenv("GITHUB_REF")?.substring(11)?.replace('/', '.') ?: "unknown"}"

		else -> "$baseVersion-build.local"
	}

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

base {
	archivesName.set(rootProject.name)
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
			memoryMaximumSize = "4G"
			// Evil witch has been here...
			// Ignores build.properties so that Manifold doesn't pick up the root properties used by IntelliJ,
			// which when loaded, breaks the build by having impossible conditions.
			jvmArgs!!.add("-javaagent:${project(":witch").layout.buildDirectory.get().asFile.resolve("libs/witch.jar")}")
		}

		doLast {
			val classes = HashMap<String, Pair<ClassNode, List<AnnotationNode>>>()

			for (file in destinationDirectory.asFileTree) {
				if (file.extension != "class") {
					continue
				}
				val node = ClassNode()
				file.inputStream().use {
					ClassReader(it).accept(node, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
				}

				val va = node.visibleAnnotations ?: listOf()
				val ia = node.invisibleAnnotations ?: listOf()
				val a = va + ia

				if (!a.any { it.desc == "Lorg/spongepowered/asm/mixin/Mixin;" }) {
					continue
				}

				classes[node.name] = node to a
			}

			println(classes)

			val source = sourceSets.first { it.output.classesDirs.contains(destinationDirectory.asFile.get()) }

			val resourceRoot = source.resources.srcDirTrees.map { it.dir }.filter { it.exists() }

			val manifest = resourceRoot
				.map { it.resolve("fabric.mod.json") }
				.firstOrNull { it.exists() }
				?.reader()?.use(JsonParser.`object`()::from) ?: return@doLast

			val mixins = (manifest.getArray("mixins") ?: listOf())
				.asSequence()
				.mapNotNull { it as? String }
				.flatMap { resourceRoot.map { res -> res.resolve(it) to it } }
				.toSet()

			println("-- nyaa --")

			mixins.forEach { (file, name) ->
				val mixin = file.reader().use(JsonParser.`object`()::from)

				val pack = mixin.getString("package").replace('.', '/')

				val applicable = classes.asSequence()
					.filter { it.key.startsWith(pack) }
					.map { it.key.substring(pack.length + 1) to it.value }
					.toMap()

				if (mixin.contains("client") || mixin.contains("server") || mixin.contains("mixins")) {
					mixin.getArray("client")?.retainAll(applicable.keys)
					mixin.getArray("server")?.retainAll(applicable.keys)
					mixin.getArray("mixins")?.retainAll(applicable.keys)

					println(applicable.keys)
					println(mixin.getArray("client"))
					println(mixin.getArray("server"))
					println(mixin.getArray("mixins"))
				} else {
					val client = JsonArray()
					val server = JsonArray()
					val others = JsonArray()

					for ((k, v) in applicable) {
						when {
							// implicit sorting via package structure
							k.startsWith("client.") -> client.add(k)
							k.startsWith("server.") -> client.add(k)
							// explicit sorting otherwise
							else -> {
								val side = v.second
									.filter { it.desc.endsWith("Environment;") || it.desc.endsWith("SideOnly;") }
									.firstNotNullOfOrNull {
										if (it.values.isNotEmpty() && it.values.size and 1 == 0) {
											it.values[it.values.indexOf("value") + 1]
										} else null
									}

								println(side)

								when ((side as? String)?.lowercase()) {
									"client" -> client.add(k)
									"server" -> server.add(k)
									null -> {
										if (side != null) println("what -> $side @ $k")
										others.add(k)
									}

									else -> {
										println("invalid -> $side @ $k")
										others.add(k)
									}
								}
							}
						}
					}

					mixin["client"] = client
					mixin["server"] = server
					mixin["mixins"] = others
				}

				val out = destinationDirectory.asFile.get().resolve(name)

				println(out)

				out.writer().use {
					JsonWriter.on(it).value(mixin).done()
				}
			}
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
				"minecraft_forge_required" to minecraftForgeRequired,
				"modrinthId" to modrinthId,
			)
		inputs.properties(map)

		filesMatching(listOf("fabric.mod.json", "quilt.mod.json", "META-INF/mods.toml")) { expand(map) }

		exclude("*.mixin.json", "*.mixins.json")
	}
	javadoc {
		(options as StandardJavadocDocletOptions).tags("reason:a:Reason")
	}
	withType<Zip> { from(rootProject.file("LICENSE")) }
}

modrinth {
	token.set(System.getenv("MODRINTH_TOKEN"))
	projectId.set(modrinthId)
	versionType.set(
		System.getenv("RELEASE_OVERRIDE") ?: when {
			"alpha" in projectVersion -> "alpha"
			!isRelease || '-' in projectVersion -> "beta"
			else -> "release"
		}
	)
	val ref = System.getenv("GITHUB_REF")
	changelog.set(
		System.getenv("CHANGELOG") ?: if (ref != null && ref.startsWith("refs/tags/")) "You may view the changelog at https://github.com/Modflower/music-moods/releases/tag/${URLEncoder.encode(ref.substring(10), StandardCharsets.UTF_8)}"
		else "No changelog is available. Perhaps poke at https://github.com/Modflower/music-moods for a changelog?"
	)
	uploadFile.set(tasks.remapJar.get())
	gameVersions.set(minecraftCompatible.split(","))
	loaders.addAll("fabric", "quilt")
}
