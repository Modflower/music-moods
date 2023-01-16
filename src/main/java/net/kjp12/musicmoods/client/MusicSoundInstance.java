/* Copyright 2023 KJP12
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package net.kjp12.musicmoods.client;// Created 2023-09-01T02:55:14

import net.minecraft.client.Timer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * Tickable music sound instance for fade out on
 *
 * @author KJP12
 * @since ${version}
 **/
public class MusicSoundInstance extends AbstractTickableSoundInstance {
	/**
	 * Maximum volume adjustment of the track allowed.
	 */
	private static final float JUMP_LIMIT = 0.025F;

	private final Timer timer = new Timer(20F, System.currentTimeMillis());
	private float fadeOut;
	private float fadeIn;

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

	@Override
	public boolean canStartSilent() {
		return this.fadeOut <= 0F && this.fadeIn > 0F;
	}

	@Override
	public boolean isStopped() {
		return super.isStopped() || (this.fadeOut > 0F && this.volume <= 0F);
	}

	public void setFadeOut(float fadeOut) {
		this.fadeOut = fadeOut;
		this.fadeIn = 0F;
	}

	public void setFadeIn(float fadeIn) {
		this.fadeIn = fadeIn;
		this.fadeOut = 0F;
	}

	@Override
	public void tick() {
		this.timer.advanceTime(System.currentTimeMillis());

		if (fadeOut > 0F && this.volume > 0F) {
			final var newVolume = Math.max(this.volume - Math.min(this.timer.tickDelta / fadeOut, JUMP_LIMIT), 0F);

			if (newVolume == newVolume) {
				this.volume = newVolume;
			}
		}

		if (fadeIn > 0F && this.volume < 1F) {
			final var newVolume = Math.min(this.volume + Math.min(this.timer.tickDelta / fadeIn, JUMP_LIMIT), 1F);

			if (newVolume == newVolume) {
				this.volume = newVolume;
			}
		}
	}

	public float getDirectVolume() {
		return this.volume;
	}

	@Override
	public String toString() {
		return "MusicSoundInstance{" + "fadeOut=" + fadeOut + ", fadeIn=" + fadeIn + ", sound=" + sound + ", location="
				+ location + ", volume=" + volume + '}';
	}
}
