import java.io.BufferedReader
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.HashMap

val ini = rootProject.file("versions.ini").toIni()

val versions = HashMap<String, String>()
val libraries = HashMap<String, Library>()

ini.sections["versions"]?.let {
	it.forEach { alias, value -> versions[alias as String] = value as String }
}

ini.sections[project.name]?.let {
	it.forEach { alias, value -> versions[alias as String] = value as String }
}

fun resolveVersions(
	properties: Properties?,
	missingVersion: (module: String, version: String) -> Unit,
	action: (alias: String, module: String, version: String) -> Unit
) {
	for ((alias, value) in properties?:return) {
		value as String

		val colon = value.lastIndexOf(':')

		val version = when {
			value[colon + 1] == '$' -> project.properties[value.substring(colon + 2)]
			value[colon + 1].isDigit() -> value.substring(colon + 1)
			else -> versions[value.substring(colon + 1)]
		} as? String

		if (version == null) {
			missingVersion(
				value.substring(0, colon),
				value.substring(colon + 1)
			)
			continue
		}

		action(
			alias as String,
			value.substring(0, colon),
			version
		)
	}
}

resolveVersions(
	ini.header,
	{ module, version -> logger.warn("Version not found for {}: {}; skipping", module, version) },
) { alias, module, version ->
	logger.info("{} => {}:{}", alias, module, version)

	val value = module.split(':')

	libraries[alias] = Library(value[0], value[1], version)
}

project.extensions.add("versions", versions)

dependencies {
	for((k, v) in ini.sections["dependencies"]?:mapOf()) {
		for(l in (v as String).split(',')
			.asSequence()
			.map(String::trim)
			.mapNotNull(libraries::get)
		) {
			(k as String)(group = l.group, name = l.name, version = l.version)
		}
	}
}

data class Library(
	val group: String,
	val name: String,
	val version: String,
) {
	override fun toString(): String {
		return "$group:$name:$version"
	}
}

// Copy from settings.gradle.kts

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
