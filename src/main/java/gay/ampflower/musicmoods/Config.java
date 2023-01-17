/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods;// Created 2022-26-12T16:26:36

import org.quiltmc.loader.api.QuiltLoader;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Ampflower
 * @since 0.0.0
 **/
public final class Config {
	private static final Path config = QuiltLoader.getConfigDir().resolve("music-moods.properties");
	private static final int fadeDefault = 600;

	/**
	 * The current config version, used for updating if needed.
	 */
	public static final int version = 0;

	/**
	 * The fade-out time in Minecraft server ticks.
	 */
	public static int fadeOutTicks = fadeDefault;

	/**
	 * The fade-in time in Minecraft server ticks.
	 */
	public static int fadeInTicks = fadeDefault;

	/**
	 * Allows the music manager to replace the current track if the situational
	 * music doesn't match the current situation.
	 */
	public static boolean allowReplacingCurrentMusic = true;

	/**
	 * Tells the music manager to always play music when replacing a track.
	 */
	public static boolean immediatelyPlayOnReplace = true;

	/**
	 * Tells the music manager to always play music.
	 */
	public static boolean alwaysPlayMusic = false;

	/**
	 * Plays music in a chaotic manner.
	 */
	public static boolean chaoticallyPlayMusic = false;

	/**
	 * Injects UI components into Vanilla for better discoverability.
	 */
	public static boolean injectUiComponents = true;

	public static void read() throws IOException {
		if (Files.notExists(config)) {
			// Commit the config, so it exists on disk to edit.
			commit();
			return;
		}

		final var properties = new Properties();
		try (final var configStream = Files.newInputStream(config)) {
			properties.load(configStream);
		}

		fadeOutTicks = toInt(properties, "fadeOutTicks", fadeDefault);
		fadeInTicks = toInt(properties, "fadeInTicks", fadeDefault);
		allowReplacingCurrentMusic = toBoolean(properties, "allowReplacingCurrentMusic", true);
		immediatelyPlayOnReplace = toBoolean(properties, "immediatelyPlayOnReplace", true);
		alwaysPlayMusic = toBoolean(properties, "alwaysPlayMusic", false);
		chaoticallyPlayMusic = toBoolean(properties, "chaoticallyPlayMusic", false);
		injectUiComponents = toBoolean(properties, "injectUiComponents", true);
	}

	public static void commit() throws IOException {
		final var properties = new Properties();

		for (final var field : Config.class.getFields())
			try {
				final var modifiers = field.getModifiers();
				if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers))
					continue;
				properties.setProperty(field.getName(), Objects.toString(field.get(null)));
			} catch (ReflectiveOperationException roe) {
				throw new AssertionError("Unexpected access violation accessing self @ " + field, roe);
			}

		Files.createDirectories(QuiltLoader.getConfigDir());

		try (final var configStream = Files.newOutputStream(config, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING)) {

			properties.store(configStream, "Music Moods Config");
		}
	}

	private static boolean toBoolean(final Properties properties, final String key, final boolean def) {
		final var str = properties.getProperty(key);
		if (str == null) {
			return def;
		}
		return Boolean.parseBoolean(str);
	}

	private static int toInt(final Properties properties, final String key, final int def) {
		final var str = properties.getProperty(key);
		if (str == null) {
			return def;
		}
		return Integer.parseInt(str);
	}
}
