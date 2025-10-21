/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods;// Created 2022-26-12T16:26:36

import gay.ampflower.musicmoods.config.Replacing;

#if FABRIC
import net.fabricmc.loader.api.FabricLoader;
#elif NEOFORGE
import net.neoforged.fml.loading.FMLLoader;
#else
import net.minecraft.client.Minecraft;
#endif

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;

/**
 * @author Ampflower
 * @since 0.0.0
 **/
public final class Config {
	private static final Path configDir;

	static {
		#if(FABRIC)
		configDir = FabricLoader.instance.configDir;
		#elif(NEOFORGE_1_21_OR_OLDER)
		configDir = FMLLoader.gamePath.resolve("config");
		#elif(NEOFORGE_1_21_9_OR_NEWER)
		configDir = FMLLoader.getCurrent().gameDir.resolve("config");
		#else
		configDir = Minecraft.instance.gameDirectory.toPath().resolve("config");
		#endif
	}

	private static final Path config = configDir.resolve("music-moods.properties");
	private static final int fadeDefault = 600;
	private static final float jukeboxReplaceRangeDefault = 0;
	private static final float jukeboxFadeRangeDefault = 48;
	private static final int soundsFadeDefault = 100;
	private static final int jukeboxFadeDefault = 20;

	/**
	 * The current config version, used for updating if needed.
	 */
	public static final int version = 1;

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
	public static Replacing situationalMusicReplacing = Replacing.allow;

	/**
	 * Tells the music manager to always play music when replacing a track.
	 */
	public static boolean immediatelyPlayOnReplace = true;

	/**
	 * Tells the music manager to always play music.
	 */
	@Deprecated(forRemoval = true)
	public static boolean alwaysPlayMusic = false;

	/**
	 * Seamlessly transition between scenes.
	 */
	public static boolean seamlessTransitions = true;

	/** Seamlessly transition between scenes, with sounds. */
	public static int seamlessSoundTransitions = 0;

	/**
	 * Allows music to be paused by vanilla.
	 */
	#if(MC_1_21_6_OR_NEWER)
	@Deprecated(forRemoval = true)#endif
	public static boolean allowPausingMusic = false;

	/**
	 * Tells the music manager how to prioritise the jukebox over situational.
	 */
	public static boolean jukeboxEnabled = true;

	/** Whether the jukebox changes affect multiplayer */
	public static boolean jukeboxMultiplayer = true;

	/**
	 * Jukebox replace range
	 *
	 * Experimental.
	 */
	public static float jukeboxReplaceRange = jukeboxReplaceRangeDefault;

	/**
	 * Jukebox fade range
	 */
	public static float jukeboxFadeRange = jukeboxFadeRangeDefault;

	/**
	 * The fade-out time in Minecraft server ticks.
	 *
	 * Note: Applies only to currently playing situational music.
	 */
	public static int jukeboxFadeMixTicks = jukeboxFadeDefault;

	/**
	 * The fade-out time in Minecraft server ticks.
	 *
	 * Note: Applies only to currently playing jukebox track.
	 */
	public static int jukeboxFadeStopTicks = jukeboxFadeDefault;

	/** Allows shift+right-clicking a disc in inventory to play it. */
	public static boolean rightClickToPlay = false;

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

		int version = toInt(properties, "version", 0);

		if (version < 1) {
			final boolean replaces = toBoolean(properties, "allowReplacingCurrentMusic", true);
			situationalMusicReplacing = replaces ? Replacing.allow : Replacing.never;
		} else {
			situationalMusicReplacing = toEnum(properties, "situationalMusicReplacing", Replacing.allow);
		}

		fadeOutTicks = toInt(properties, "fadeOutTicks", fadeDefault);
		fadeInTicks = toInt(properties, "fadeInTicks", fadeDefault);
		immediatelyPlayOnReplace = toBoolean(properties, "immediatelyPlayOnReplace", true);
		alwaysPlayMusic = toBoolean(properties, "alwaysPlayMusic", false);

		seamlessTransitions = toBoolean(properties, "seamlessTransitions", true);
		allowPausingMusic = toBoolean(properties, "allowPausingMusic", false);

		jukeboxEnabled = toBoolean(properties, "jukeboxEnabled", true);
		jukeboxMultiplayer = toBoolean(properties, "jukeboxMultiplayer", true);
		jukeboxReplaceRange = toFloat(properties, "jukeboxReplaceRange", jukeboxReplaceRangeDefault);
		jukeboxFadeRange = toFloat(properties, "jukeboxFadeRange", jukeboxFadeRangeDefault);
		jukeboxFadeMixTicks = toInt(properties, "jukeboxFadeMixTicks", jukeboxFadeDefault);
		jukeboxFadeStopTicks = toInt(properties, "jukeboxFadeStopTicks", jukeboxFadeDefault);

		rightClickToPlay = toBoolean(properties, "rightClickToPlay", false);

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
				final var value = field.get(null);
				if (value != null) {
					properties.setProperty(field.getName(), Objects.toString(value));
				}
			} catch (ReflectiveOperationException roe) {
				throw new AssertionError("Unexpected access violation accessing self @ " + field, roe);
			}

		Files.createDirectories(configDir);

		try (final var configStream = Files.newOutputStream(config, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING)) {

			properties.store(configStream, "Music Moods Config");
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends Enum<T>> T toEnum(final Properties properties, final String key, final T def) {
		final var str = properties.getProperty(key);
		if (str == null) {
			return def;
		}
		for (final var t : def.getClass().getEnumConstants()) {
			if (t.name().equals(str)) {
				return (T) t;
			}
		}
		return def;
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

	private static float toFloat(final Properties properties, final String key, final float def) {
		final var str = properties.getProperty(key);
		if (str == null) {
			return def;
		}
		return Float.parseFloat(str);
	}

	private static <T> T toMaybe(final Properties properties, final String key, final Function<String, T> converter) {
		final var str = properties.getProperty(key);
		if (str == null) {
			return null;
		}
		return converter.apply(str);
	}
}
