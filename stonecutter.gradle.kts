import java.nio.file.Files
import java.util.Properties

plugins {
	id("dev.kikugie.stonecutter")
}

stonecutter active "forge-1.20"

stonecutter.current?.apply {
	val manifold = file("versions/$project/build.properties").toPath()
	val current = file("build.properties").toPath()

	if (Files.notExists(current) || manifold != current) {
		Files.deleteIfExists(current)
		Files.createLink(current, manifold)
	}
}

stonecutter.versions.forEach {
	val manifold = file("versions/${it.project}/build.properties")
	val properties = Properties()

	manifold.reader().use(properties::load)

	project(it.project) {
		beforeEvaluate {
			extensions.add("manifold", properties)
		}
	}
}
