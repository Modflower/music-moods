import com.modrinth.minotaur.dependencies.DependencyType
import com.modrinth.minotaur.dependencies.ModDependency
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

plugins {
	base
	id("com.modrinth.minotaur")
}

val minecraft: String by project
val minecraftCompatible: String by project

val loaders: String? by project

val modrinthId: String by project
val projectVersion: String by project

val isPublish = System.getenv("GITHUB_EVENT_NAME") == "release"
val isRelease = System.getenv("BUILD_RELEASE").toBoolean()
val isActions = System.getenv("GITHUB_ACTIONS").toBoolean()
val baseVersion: String = "$projectVersion+mc.${minecraft}"

version =
	when {
		isRelease -> baseVersion
		isActions ->
			"$baseVersion-build.${System.getenv("GITHUB_RUN_NUMBER")}-commit.${System.getenv("GITHUB_SHA").substring(0, 7)}-branch.${System.getenv("GITHUB_REF")?.substring(11)?.replace('/', '.') ?: "unknown"}"

		else -> "$baseVersion-build.local"
	}

base {
	archivesName.set(rootProject.name)
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
	gameVersions.set(minecraftCompatible.split(","))

	(project.properties["loaders"] as? String)?.let {
		loaders.addAll(it.split(','))
	}

	(project.properties["dependencies.required"] as? String)?.let {
		for (dep in it.split(',')) {
			dependencies.add(ModDependency(dep, DependencyType.REQUIRED))
		}
	}

	(project.properties["dependencies.embedded"] as? String)?.let {
		for (dep in it.split(',')) {
			dependencies.add(ModDependency(dep, DependencyType.EMBEDDED))
		}
	}
}
