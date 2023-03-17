/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

rootProject.name = "music-moods"

pluginManagement {
	repositories {
		maven("Quilt") {
			url = uri("https://maven.quiltmc.org/repository/release")
		}
		maven("Fabric") {
			url = uri("https://maven.fabricmc.net/")
		}
		gradlePluginPortal()
	}
}
