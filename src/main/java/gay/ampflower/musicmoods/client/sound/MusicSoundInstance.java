/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.client.sound;// Created 2023-09-01T02:55:

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * Tickable music sound instance for fade out on
 *
 * @author Ampflower
 * @since 0.0.0
 **/
public class MusicSoundInstance extends FadeableSoundInstance {

	public MusicSoundInstance(final SoundEvent soundEvent) {
		super(soundEvent, SoundSource.MUSIC, SoundInstance.createUnseededRandom());

		this.relative = true;
	}

	public MusicSoundInstance(final SoundEvent soundEvent, final float fadeIn) {
		this(soundEvent);

		if (fadeIn > 0F) {
			this.setFadeIn(fadeIn);
			this.volume = 0F;
		}
	}

	public float getDirectVolume() {
		return this.volume;
	}

	@Override
	public String toString() {
		return "MusicSoundInstance{" + "fadeOut=" + fadeOut + ", fadeIn=" + fadeIn + ", sound=" + sound + ", location="
			   + #if (MC_1_21_11_OR_NEWER) identifier #else location #endif + ", volume=" + volume + '}';
	}
}
