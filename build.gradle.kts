/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

plugins {
	java
	`java-library`
	alias(libs.plugins.loom)
	`maven-publish`
	alias(libs.plugins.spotless)
}

val projectVersion: String by project

val isPublish = System.getenv("GITHUB_EVENT_NAME") == "release"
val isRelease = System.getenv("BUILD_RELEASE").toBoolean()
val isActions = System.getenv("GITHUB_ACTIONS").toBoolean()
val baseVersion: String = "$projectVersion+mc.${libs.versions.minecraft.version.get()}"

version =
	when {
		isRelease -> baseVersion
		isActions ->
			"$baseVersion-build.${System.getenv("GITHUB_RUN_NUMBER")}-commit.${System.getenv("GITHUB_SHA").substring(0, 7)}-branch.${System.getenv("GITHUB_REF")?.substring(11)?.replace('/', '.') ?: "unknown"}"

		else -> "$baseVersion-build.local"
	}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
	withSourcesJar()
	withJavadocJar()
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
	minecraft(libs.minecraft)
	mappings(loom.officialMojangMappings())
	modImplementation(libs.quilt.loader)
	modImplementation(libs.spruceui)?.let(::include)
	modImplementation(libs.modmenu)
	modImplementation(libs.quilted.fabric.api)
	modRuntimeOnly(libs.bundles.mod.runtime)
}

spotless {
	val licenseHeader = rootDir.resolve(".internal/license-header.java")
	java {
		importOrderFile(rootDir.resolve(".internal/spotless.importorder"))

		// If the spotless config doesn't exist, this will fall back to the eclipse default.
		val eclipse = eclipse()
		val eclipseConfig = rootDir.resolve(".internal/spotless.xml")
		if (eclipseConfig.exists()) eclipse.configFile(eclipseConfig)

		// If the license header doesn't exist, it'll simply not be applied.
		if (licenseHeader.exists()) licenseHeaderFile(licenseHeader)
	}
	kotlinGradle {
		target("*.gradle.kts")
		if (licenseHeader.exists()) licenseHeaderFile(licenseHeader, "(import|plugins|rootProject)")
	}
}

tasks {
	withType<JavaCompile> {
		options.encoding = "UTF-8"
		options.isDeprecation = true
		options.isWarnings = true
	}
	processResources {
		val map =
			mapOf(
				"id" to project.name,
				"java" to java.targetCompatibility.majorVersion,
				"version" to project.version,
				"project_version" to projectVersion,
				"minecraft_required" to libs.versions.minecraft.required.get()
			)
		inputs.properties(map)

		filesMatching("quilt.mod.json") { expand(map) }
	}
	withType<Jar> { from("LICENSE") }
}
