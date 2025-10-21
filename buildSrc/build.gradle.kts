plugins {
	`kotlin-dsl`
}

repositories {
	gradlePluginPortal()
}

dependencies {
	implementation("com.modrinth.minotaur:com.modrinth.minotaur.gradle.plugin:2.+")

	implementation("com.grack:nanojson:1.10")
	implementation("org.ow2.asm:asm:9.9")
}
