/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
#if MC_1_18_OR_NEWER
import net.minecraft.core.Holder;
#endif
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

#if MC_1_21_OR_NEWER
import gay.ampflower.musicmoods.Sounds;
import net.minecraft.world.item.JukeboxSong;
#endif

#if MC_1_21_4_OR_NEWER && !MC_1_21_11_OR_NEWER
#define MUSIC_INFO
import net.minecraft.client.sounds.MusicInfo;
#else
import net.minecraft.sounds.Music;
#endif


/**
 * @author Ampflower
 * @since 0.6.12
 **/
public interface MusicHandler {
	#if MC_1_21_OR_NEWER

	/**
	 * Intrudes the given jukebox song into the music manager.
	 *
	 * @param song The jukebox song to intrude.
	 * @return Whether it swapped out the track. {@code false} means it is already playing.
	 */
	default boolean moods$intrudeJukeboxTrack(final @NotNull JukeboxSong song) {
		final Holder<SoundEvent> soundEvent = Sounds.findStereo(song.soundEvent());

		return moods$intrudeJukeboxTrack(soundEvent, song.description());
	}

	default boolean moods$isCurrentlyPlaying(final @NotNull JukeboxSong song) {
		final Holder<SoundEvent> soundEvent = Sounds.findStereo(song.soundEvent());

		return moods$isCurrentlyPlaying(soundEvent.value());
	}
	#endif

	#if MC_1_17_OR_OLDER
	default boolean moods$intrudeJukeboxTrack(
		final @NotNull SoundEvent soundEvent
	) {
		return moods$intrudeJukeboxTrack(soundEvent, null);
	}

	boolean moods$intrudeJukeboxTrack(
		final @NotNull SoundEvent soundEvent,
		final @Nullable Component name
	);
	#else
	default boolean moods$intrudeJukeboxTrack(
		final @NotNull SoundEvent soundEvent
	) {
		return moods$intrudeJukeboxTrack(Holder.direct(soundEvent), null);
	}

	default boolean moods$intrudeJukeboxTrack(
		final @NotNull Holder<SoundEvent> soundEvent
	) {
		return moods$intrudeJukeboxTrack(soundEvent, null);
	}

	boolean moods$intrudeJukeboxTrack(
		final @NotNull Holder<SoundEvent> soundEvent,
		final @Nullable Component name
	);
	#endif

	#if MUSIC_INFO
	boolean moods$intrudeMusic(
		final @NotNull MusicInfo music
	);
	#else
	boolean moods$intrudeMusic(
		final @NotNull Music music
	);
	#endif

	boolean moods$isCurrentlyPlaying(final SoundEvent soundEvent);

	/**
	 * Removes a track from the intruding or potential lists.
	 *
	 * @param instance The sound instance that was stopped. Passed in by the sound engine.
	 * @implNote A return value whether the instance was removed is impossible,
	 * 	due to not knowing whether the sound engine is ticked off-thread or not.
	 * @since 0.7.0
	 */
	@ApiStatus.Internal
	void moods$removeProbableIntrusion(final SoundInstance instance);

	/**
	 * Adds a track that's on the {@link SoundSource#MUSIC music sound source}
	 * or {@link SoundSource#RECORDS jukebox sound source} to the intrusion list,
	 * only if it isn't the music manager's sound instance.
	 * <p>
	 * This may include non-music tracks that are in stereo, due to the nature of such tracks.
	 * <p>
	 * The {@code isGlobal} parameter is only a hint; the music manager may choose to treat the provided
	 * sound instance as if it was global anyway.
	 *
	 * @param instance The sound instance being intruded. Passed in by the sound engine.
	 * @param isGlobal Hint of whether the sound is either stereo, or is positioned on top of the player.
	 * @implNote A return value whether the instance was added is impossible,
	 * 	due to not knowing whether the sound engine is ticked off-thread or not.
	 * @since 0.7.0
	 */
	@ApiStatus.Internal
	void moods$addProbableIntrusion(final SoundInstance instance, final boolean isGlobal);

	Component moods$getCurrentMusicName();

	@ApiStatus.Internal
	void moods$reload();

	static MusicHandler getInstance() {
		return (MusicHandler)Minecraft.getInstance().musicManager;
	}
}
