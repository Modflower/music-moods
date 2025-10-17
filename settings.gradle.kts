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

val versions = file("versions.ini").toIni()
val stonecutterIni = file("stonecutter.ini").toIni()

dependencyResolutionManagement
	.versionCatalogs
	.create("libs") {
		val map = HashMap<String, String>()

		versions.sections["versions"]?.let {
			it.forEach { alias, value -> map[alias as String] = value as String }
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
					value[colon + 1] == '$' -> stonecutterIni.header[value.substring(colon + 2)]
					value[colon + 1].isDigit() -> value.substring(colon + 1)
					else -> map[value.substring(colon + 1)]
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

		resolveVersions(versions.sections["plugins"], { throw NoSuchElementException("version $it") }) { alias, module, version ->
			logger.info("{} => {}:{}", alias, module, version)
			plugin(alias, module).version(version)
		}

		resolveVersions(versions.header, {}) { alias, module, version ->
			logger.info("{} => {}:{}", alias, module, version)

			val value = module.split(':')

			library(alias, value[0], value[1]).version(version)
		}

		for ((alias, version) in map) {
			version(alias, version)
		}
	}

stonecutterIni.let { ini ->
	stonecutter {
		create(rootProject) {
			versions(ini.sections.keys)
			vcsVersion = ini.getHeader("vcs_version")!!
		}
	}

	val stonecutterRoot = file("versions")
	val stonecutterLastModified = Files.getLastModifiedTime(file("stonecutter.ini").toPath())!!

	for ((k, v) in ini.sections) {
		val root = stonecutterRoot.resolve(k)
		root.mkdirs()

		root.resolve("gradle.properties")
			.updateOnMismatch(v, stonecutterLastModified, "== DO NOT MODIFY ==\n\nSee stonecutter.ini instead")

		val buildProperties = Properties()

		for ((k, v) in v) {
			buildProperties[(k as String).toYellingSnake()] = v
		}

		buildProperties["MC_" + k.toYellingSnake()] = "true"

		for (v in ini.sections.keys) {
			val c = FlexVerComparator.compare(k, v)
			val snake = v.toYellingSnake()

			if (c <= 0) {
				buildProperties["MC_${snake}_OR_OLDER"] = "true"
			}

			if (c >= 0) {
				buildProperties["MC_${snake}_OR_NEWER"] = "true"
			}
		}

		root.resolve("build.properties")
			.updateOnMismatch(buildProperties, stonecutterLastModified)
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

inline fun <reified K, reified V> forwardTo(src: Properties, dest: MutableMap<K, V>) =
	src.forEach { k, v -> dest[k as K] = v as V }
