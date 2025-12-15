package moe.amp

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonWriter
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.slf4j.LoggerFactory

import java.io.File

typealias MixinClass = Pair<ClassNode, List<AnnotationNode>>
typealias MixinMap = HashMap<String, MixinClass>

/**
 * @author Ampflower
 * @since 0.7
 **/
object AutoMixin {

	private val logger = LoggerFactory.getLogger(AutoMixin::class.java)

	fun SourceSetContainer.findFirst(destination: File): SourceSet = first { it.output.classesDirs.contains(destination) }

	val SourceSet.resourceRoots
		get() = resources.srcDirTrees.map { it.dir }.filter { it.exists() }

	fun List<File>.resolve(path: String) = map { it.resolve(path) }
		.filter { it.exists() }

	fun List<File>.resolveFirst(path: String) = map { it.resolve(path) }
		.firstOrNull { it.exists() }

	fun resolveMixinClasses(tree: FileTree): MixinMap {
		val classes = MixinMap()

		for (file in tree) {
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

		logger.info("Classes: {}", classes)

		return classes
	}

	fun getSide(annotations: List<AnnotationNode>): Side {
		val side = annotations
			.filter { it.desc.endsWith("Environment;") || it.desc.endsWith("SideOnly;") }
			.firstNotNullOfOrNull {
				if (it.values.isNotEmpty() && it.values.size and 1 == 0) {
					it.values[it.values.indexOf("value") + 1]
				} else null
			}

		if (side as? String != null) {
			when(side.lowercase()) {
				"client" -> return Side.CLIENT
				"server" -> return Side.SERVER
			}
		}
		if (side != null) {
			logger.warn("Unknown value: {}", side)
		}
		return Side.COMMON
	}

	fun getSide(mixin: MixinClass, name: String): Side {
		val side = getSide(mixin.second)

		if (side != Side.COMMON) {
			return side
		}

		return when {
			name.startsWith("client.") -> Side.CLIENT
			name.startsWith("server.") -> Side.SERVER
			else -> Side.COMMON
		}
	}

	fun resolveMixinConfigs(files: List<File>, vararg mixins: String): Map<File, String> {
		return mixins
			.asSequence()
			.flatMap { files.map { res -> res.resolve(it) to it } }
			.toMap()
	}

	fun resolveMixinConfigs(files: List<File>, mixins: List<Any?>?): Map<File, String> {
		if (mixins == null) {
			return mapOf()
		}

		return mixins
			.asSequence()
			.mapNotNull { it as? String }
			.flatMap { files.map { res -> res.resolve(it) to it } }
			.toMap()
	}

	fun resolveMixinConfigsByFabric(files: List<File>): Map<File, String>? {
		val manifest = files.resolveFirst("fabric.mod.json")
			?.reader()?.use(JsonParser.`object`()::from) ?: return null

		return resolveMixinConfigs(files, manifest.getArray("mixins") ?: listOf())
	}

	private fun filterJsonArray(array: JsonArray?, filter: Set<String>, name: String) {
		if (array == null) {
			return
		}

		if (logger.isInfoEnabled) {
			logger.info("{}:", name)
			for (i in array) {
				logger.info("\t- {}", i)
			}
		}

		array.retainAll(filter)
	}

	fun filterMixins(mixin: JsonObject, classes: Lazy<MixinMap>) {
		val pack = mixin.getString("package").replace('.', '/')

		val applicable = classes.value.asSequence()
			.filter { it.key.startsWith(pack) }
			.map { it.key.substring(pack.length + 1).replace('/', '.') to it.value }
			.toMap()

		logger.info("Applicable:\n\t- {}", java.lang.String.join("\n\t- ", applicable.keys))

		// Simple retain all if any of the trio are present.
		if (mixin.contains("client") || mixin.contains("server") || mixin.contains("mixins")) {
			filterJsonArray(mixin.getArray("client"), applicable.keys, "client")
			filterJsonArray(mixin.getArray("server"), applicable.keys, "server")
			filterJsonArray(mixin.getArray("mixins"), applicable.keys, "mixins")
			return
		}

		val client = JsonArray()
		val server = JsonArray()
		val others = JsonArray()

		for ((k, v) in applicable) {
			val side = getSide(v, k)
			logger.info("Side({}, {}) = {}", v, k, side)
			when (side) {
				Side.CLIENT -> client
				Side.SERVER -> server
				Side.COMMON -> others
			}.add(k)
		}

		mixin["client"] = client
		mixin["server"] = server
		mixin["mixins"] = others
	}

	fun JavaCompile.autoMixinGeneric(
		sourceSets: SourceSetContainer,
		vararg mixinFiles: String
	) {
		val classes = lazy { resolveMixinClasses(destinationDirectory.asFileTree) }

		val resourceRoot = sourceSets.findFirst(destinationDirectory.asFile.get()).resourceRoots

		val mixins = resolveMixinConfigs(resourceRoot, *mixinFiles)

		mixins.forEach { (file, name) ->
			val mixin = file.reader().use(JsonParser.`object`()::from)

			filterMixins(mixin, classes)

			val out = destinationDirectory.asFile.get().resolve(name)

			out.writer().use {
				JsonWriter.on(it).value(mixin).done()
			}
		}
	}

	fun JavaCompile.autoMixinFabric(
		sourceSets: SourceSetContainer,
	) {
		val classes = lazy { resolveMixinClasses(destinationDirectory.asFileTree) }

		val resourceRoot = sourceSets.findFirst(destinationDirectory.asFile.get()).resourceRoots

		val mixins = resolveMixinConfigsByFabric(resourceRoot) ?: return

		mixins.forEach { (file, name) ->
			val mixin = file.reader().use(JsonParser.`object`()::from)

			filterMixins(mixin, classes)

			val out = destinationDirectory.asFile.get().resolve(name)

			out.writer().use {
				JsonWriter.on(it).value(mixin).done()
			}
		}
	}

	enum class Side {
		CLIENT, SERVER, COMMON
	}
}
