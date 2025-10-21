import moe.amp.Library
import moe.amp.ini.*
import java.io.BufferedReader
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.HashMap

plugins {
	`java-base`
}

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

project.extensions.add(
	(object : TypeOf<Map<String, Library>>(){}),
	"libraries",
	libraries
)

dependencies {
	fun meow(k: Any, v: Any) {
		for(l in (v as String).split(',')
			.asSequence()
			.map(String::trim)
			.mapNotNull(libraries::get)
		) {
			(k as String)(group = l.group, name = l.name, version = l.version)
		}
	}

	for((k, v) in ini.sections["dependencies"]?:mapOf()) { meow(k, v) }
	for((k, v) in ini.sections[project.name.split('-')[0]]?:mapOf()) { meow(k, v) }
}
