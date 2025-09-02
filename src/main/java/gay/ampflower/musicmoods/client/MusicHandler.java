/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.client;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ampflower
 * @since 0.6.12
 **/
public interface MusicHandler {
	/**
	 * Intrudes the given jukebox song into the music manager.
	 *
	 * @param jukeboxSong The jukebox song to intrude.
	 * @return Whether it swapped out the track. {@code false} means it is already playing.
	 */
	boolean moods$intrudeJukeboxTrack(final @NotNull Holder<JukeboxSong> jukeboxSong);

	default boolean moods$isCurrentlyPlaying(final @NotNull Holder<JukeboxSong> jukeboxSong) {
		return moods$isCurrentlyPlaying(jukeboxSong.value().soundEvent().value());
	}

	boolean moods$isCurrentlyPlaying(final SoundEvent soundEvent);

	Component moods$getCurrentMusicName();
}
