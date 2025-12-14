/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.client;

#if MC_1_18_OR_NEWER
import net.minecraft.core.Holder;
#endif
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

#if MC_1_21_OR_NEWER
import gay.ampflower.musicmoods.Sounds;
import net.minecraft.world.item.JukeboxSong;
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

	boolean moods$isCurrentlyPlaying(final SoundEvent soundEvent);

	Component moods$getCurrentMusicName();
}
