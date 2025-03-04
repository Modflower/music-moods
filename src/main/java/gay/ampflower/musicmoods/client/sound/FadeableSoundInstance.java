/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.client.sound;

import net.minecraft.client.Timer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * @author Ampflower
 * @since 0.5
 **/
public class FadeableSoundInstance extends AbstractTickableSoundInstance implements Fadeable {
	/**
	 * Maximum volume adjustment of the track allowed.
	 */
	private static final float JUMP_LIMIT = 0.1F;

	protected final Timer timer = new Timer(20F, System.currentTimeMillis());
	protected float maxVolume = 1.f;
	protected float fadeOut;
	protected float fadeIn;

	protected FadeableSoundInstance(final SoundEvent soundEvent, final SoundSource soundSource,
			final RandomSource randomSource) {
		super(soundEvent, soundSource, randomSource);
	}

	@Override
	public void tick() {
		this.timer.advanceTime(System.currentTimeMillis());

		if (fadeOut > 0F && this.volume > 0F) {

			if (this.volume > 1F) {
				this.volume /= 2;
			}

			final var newVolume = Math.max(this.volume - Math.min(this.timer.tickDelta / fadeOut, JUMP_LIMIT), 0F);

			if (newVolume == newVolume) {
				this.volume = newVolume;
			}
		}

		if (fadeIn > 0F && this.volume < maxVolume) {
			final var newVolume = Math.min(this.volume + Math.min(this.timer.tickDelta / fadeIn, JUMP_LIMIT),
					maxVolume);

			if (newVolume == newVolume) {
				this.volume = newVolume;
			}
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

	@Override
	public void setFadeOut(float fadeOut) {
		this.fadeOut = fadeOut;
		this.fadeIn = 0F;
	}

	@Override
	public void setFadeIn(float fadeIn) {
		this.fadeIn = fadeIn;
		this.fadeOut = 0F;
	}
}
