/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import com.unascribed.flexver.FlexVerComparator
import java.io.BufferedReader
import java.io.Writer
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.util.Properties

rootProject.name = "music-moods"

pluginManagement {
	repositories {
		maven("https://maven.fabricmc.net/") { name = "FabricMC" }
		maven("https://maven.neoforged.net/releases/") { name = "Neoforged" }
		maven("https://maven.architectury.dev/") { name = "Architectury" }
		gradlePluginPortal()
		maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
	}
}

plugins {
	id("dev.kikugie.stonecutter") version "0.7.10"
}

buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("com.unascribed:flexver-java:1.1.1")
	}
}

include("witch")

val versions = Versions(file("versions.ini").toIni())

val gradleProperties = file("gradle.properties").toProperties()

dependencyResolutionManagement.versionCatalogs {
	create("libs") {
		versions.resolvePlugins(this) { throw NoSuchElementException("version $it") }

		versions.resolveVersions(this, null) {}

		for ((alias, version) in versions.versions) {
			version(alias, version)
		}
	}
}

stonecutter {
	create(rootProject) {
		for ((loader, ini) in versions.cutters) {
			for (k in ini.sections.keys) {
				version("$loader-$k", k).apply {
					buildscript = ini.header["buildscript"] as? String ?: "$loader.gradle.kts"
				}
			}
		}
		vcsVersion = gradleProperties.getProperty("vcs_version")!!
	}
}

val stonecutterRoot = file("versions")
for ((loader, ini) in versions.cutters) {
	val stonecutterLastModified = maxOf(
		Files.getLastModifiedTime(file("$loader.ini").toPath())!!,
		Files.getLastModifiedTime(file("versions.ini").toPath())!!,
	)

	val yellLoader = loader.toYellingSnake()

	for ((k, v) in ini.sections) {
		val root = stonecutterRoot.resolve("$loader-$k")
		root.mkdirs()

		val gradle = Properties()
		gradle.putAll(ini.header)
		gradle.putAll(v)

		root.resolve("gradle.properties")
			.updateOnMismatch(gradle, stonecutterLastModified, "== DO NOT MODIFY ==\n\nSee versions.ini instead")

		val buildProperties = Properties()

		for ((k, v) in v) {
			buildProperties[(k as String).toYellingSnake()] = v
		}

		if (!buildProperties.containsKey(yellLoader)) {
			buildProperties[yellLoader] = "true"
		}

		vcomp(buildProperties, k, versions.splices)
		vcomp(buildProperties, k, ini.sections.keys, loader.uppercase())

		root.resolve("build.properties")
			.updateOnMismatch(buildProperties, stonecutterLastModified)
	}
}

fun vcomp(
	buildProperties: Properties,
	reference: String,
	splices: Set<String>,
	prefix: String = "MC",
) {
	buildProperties["${prefix}_${reference.toYellingSnake()}"] = "true"

	for (v in splices) {
		val c = FlexVerComparator.compare(reference, v)
		val snake = v.toYellingSnake()

		if (c <= 0) {
			buildProperties["${prefix}_${snake}_OR_OLDER"] = "true"
		}

		if (c >= 0) {
			buildProperties["${prefix}_${snake}_OR_NEWER"] = "true"
		}
	}
}

class Versions(
	val ini: Ini,
) {
	val cutters: Map<String, Ini>

	val splices: Set<String>

	val versions: Map<String, String> = HashMap<String, String>().apply {
		ini.sections["versions"]?.let {
			it.forEach { alias, value -> this[alias as String] = value as String }
		}
	}

	init {
		val cutters = HashMap<String, Ini>()
		val splices = HashSet<String>()
		for (k in HashSet<String>().apply { addAll(ini.sections.keys); removeAll(reservedSections) }) {
			val ini = file("$k.ini").toIni()
			cutters[k] = ini
			splices.addAll(ini.sections.keys)
		}
		this.cutters = cutters
		this.splices = splices
	}

	fun resolveVersions(
		properties: Properties?,
		missingVersion: (version: String) -> Unit,
		action: (alias: String, module: String, version: String) -> Unit
	) {
		for ((alias, value) in properties ?: return) {
			value as String

			val colon = value.lastIndexOf(':')

			val version = when {
				value[colon + 1] == '$' -> gradleProperties[value.substring(colon + 2)]
				value[colon + 1].isDigit() -> value.substring(colon + 1)
				else -> versions[value.substring(colon + 1)]
			} as? String

			if (version == null) {
				missingVersion(value.substring(colon + 1))
				continue
			}

			action(
				alias as String,
				value.substring(0, colon),
				version
			)
		}
	}

	fun resolvePlugins(
		builder: VersionCatalogBuilder,
		missingVersion: (version: String) -> Unit,
	) {
		resolveVersions(ini.sections["plugins"], missingVersion) { alias, module, version ->
			logger.info("{} => {}:{}", alias, module, version)

			builder.plugin(alias, module).version(version)
		}
	}

	fun resolveVersions(
		builder: VersionCatalogBuilder,
		section: String?,
		missingVersion: (version: String) -> Unit,
	) {
		val properties = if (section == null) {
			ini.header
		} else {
			ini.sections[section] ?: throw NoSuchElementException("not found: $section")
		}

		resolveVersions(properties, missingVersion) { alias, module, version ->
			logger.info("{} => {}:{}", alias, module, version)

			val value = module.split(':')

			builder.library(alias, value[0], value[1]).version(version)
		}
	}

	companion object {
		val reservedSections = setOf("plugins", "versions", "dependencies")
	}
}

fun File.updateOnMismatch(expected: Properties, lastModified: FileTime, message: String? = null) {
	whenMismatch(expected, lastModified) { expected.store(it, message) }
}

fun File.whenMismatch(expected: Properties, lastModified: FileTime, action: (Writer) -> Unit) {
	if (matches(this, expected, lastModified)) {
		return
	}

	this.bufferedWriter().use(action)
}

fun matches(properties: File, expected: Properties, lastModified: FileTime): Boolean {
	if (!properties.exists()) {
		return false
	}

	val propertiesPath = properties.toPath()

	// Microcache
	if (Files.getLastModifiedTime(propertiesPath) >= lastModified) {
		return true
	}

	val temp = Properties()

	properties.bufferedReader().use(temp::load)

	// I'd hate to do needless writes on SSDs.
	if (temp == expected) {
		Files.setLastModifiedTime(propertiesPath, lastModified)
		return true
	}
	return false
}

fun String.toYellingSnake(): String {
	val builder = StringBuilder()

	for (i in indices) {
		val char = this[i]
		if (char.isUpperCase() && i != 0) {
			builder.append('_')
		}

		builder.append(
			when (char) {
				'.', ',', '-' -> '_'
				else -> char.uppercaseChar()
			}
		)
	}

	return builder.toString()
}

// Pretty much a Windows-spec ini parser with the concept of a null header section.
// Why here? There's not a buildSrc for settings.gradle.kts, so this is the best place to shove this,
// outside of making a plugin.
data class Ini(val header: Properties, val sections: HashMap<String, Properties>) {
	companion object {
		fun read(reader: BufferedReader): Ini {
			val sections = HashMap<String, Properties>()
			val nullSection = Properties()
			var properties = nullSection
			var line: String?

			while (reader.readLine().apply { line = this?.trim() } != null) {
				val line = line
				if (line.isNullOrEmpty()) {
					continue
				}
				if (line.startsWith(';')) {
					continue
				}
				if (line.startsWith('[') && line.endsWith(']')) {
					val section = line.substring(1, line.length - 1)
					properties = Properties()
					sections.put(section, properties)
					continue
				}
				val equals = line.indexOf('=')

				if (equals >= 0) {
					properties.put(
						line.substring(0, equals).trimEnd(),
						line.substring(equals + 1).trimStart()
					)
				}
			}

			return Ini(nullSection, sections)
		}
	}

	fun getHeader(key: String) = header[key] as String?

	fun get(section: String, key: String) = sections[section]?.get(key) as String?
}

fun File.toIni(): Ini = bufferedReader().use { Ini.read(it) }

fun java.nio.file.Path.toIni(): Ini = Files.newBufferedReader(this).use { Ini.read(it) }

fun File.toProperties(): Properties = bufferedReader().use { Properties().apply { load(it) } }

inline fun <reified K, reified V> forwardTo(src: Properties, dest: MutableMap<K, V>) =
	src.forEach { k, v -> dest[k as K] = v as V }
