package moe.amp.ini

import java.io.BufferedReader
import java.io.File
import java.util.*
import kotlin.collections.HashMap

/**
 * Pretty much a Windows-spec ini parser with the concept of a null header section.
 * Why here? There's not a buildSrc for settings.gradle.kts, so this is the best place to shove this,
 * outside of making a plugin.
 *
 * @author Ampflower
 * @since 0.7
 **/
// Copy from settings.gradle.kts
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
