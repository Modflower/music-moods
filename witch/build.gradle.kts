plugins {
	java
	alias(libs.plugins.shadow)
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(libs.asm)
	implementation(libs.asm.tree)
	compileOnly(libs.annotations)
}

tasks {
	jar {
		enabled = false
	}
	shadowJar {
		archiveClassifier = null
		relocate("org.objectweb.asm", "moe.amp.witch.asm")
		manifest {
			attributes("Premain-Class" to "moe.amp.witch.Witch")
		}
	}
}
