plugins {
	java
	application
	alias(libs.plugins.shadow)
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(libs.asm)
	implementation(libs.asm.tree)
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
