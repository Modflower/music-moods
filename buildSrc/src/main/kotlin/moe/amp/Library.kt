package moe.amp

data class Library(
	val group: String,
	val name: String,
	val version: String,
) {
	override fun toString(): String {
		return "$group:$name:$version"
	}
}
