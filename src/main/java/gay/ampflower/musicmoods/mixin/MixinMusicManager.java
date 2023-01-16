/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;// Created 2022-24-12T20:34:50

import gay.ampflower.musicmoods.Config;
import gay.ampflower.musicmoods.client.MusicSoundInstance;
import gay.ampflower.musicmoods.client.WeighedSoundEventsQuery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Ampflower
 * @since ${version}
 **/
@Mixin(MusicManager.class)
public abstract class MixinMusicManager {
	@Shadow
	@Nullable
	private SoundInstance currentMusic;
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	private int nextSongDelay;
	@Shadow
	@Final
	private RandomSource random;

	@Shadow
	public abstract void startPlaying(final Music music);

	private MusicSoundInstance fadingOutMusic;
	private ResourceLocation currentCompatibleLocation;

	/**
	 * @author Ampflower
	 * @reason Better integration without having injects and redirects everywhere.
	 */
	@Overwrite
	public void tick() {
		final var music = this.minecraft.getSituationalMusic();
		final var musicLocation = music.getEvent().getLocation();
		// Cache the sound manager in a local.
		final var soundManager = this.minecraft.getSoundManager();
		var oldFadingOutMusic = this.fadingOutMusic;

		if (oldFadingOutMusic != null && !soundManager.isActive(oldFadingOutMusic)) {
			this.fadingOutMusic = oldFadingOutMusic = null;
		}

		if (this.currentMusic != null) {

			// Allow the end user to say whether they want their music replaced at all.
			if (Config.allowReplacingCurrentMusic && (isLoudAndCompatible(oldFadingOutMusic, musicLocation)
					|| (music.replaceCurrentMusic() && isReplaceable(this.currentMusic, musicLocation)))) {

				// Do a fade out on the current music if configured to make it not jarring.
				if (Config.fadeOutTicks > 0 && this.currentMusic instanceof MusicSoundInstance musicSoundInstance) {
					musicSoundInstance.setFadeOut(Config.fadeOutTicks);
					this.fadingOutMusic = musicSoundInstance;
				} else {
					soundManager.stop(this.currentMusic);
				}

				this.currentCompatibleLocation = null;

				if (oldFadingOutMusic != null && isCompatible(oldFadingOutMusic, musicLocation)) {
					this.currentMusic = oldFadingOutMusic;
					oldFadingOutMusic.setFadeIn(Config.fadeInTicks);
				} else if (Config.immediatelyPlayOnReplace) {
					this.startPlayingFadeIn(music);
				} else {
					// Clear currentMusic, so it's not trying to tick it.
					this.currentMusic = null;
					this.currentCompatibleLocation = null;

					// Set the delay, since the old track is not applicable to move in.
					this.nextSongDelay = Mth.nextInt(this.random, 0, music.getMinDelay() / 2);
				}
			}

			if (!soundManager.isActive(this.currentMusic)) {
				this.currentMusic = null;
				this.currentCompatibleLocation = null;

				// Change from Mojang code: Removed the Math.min as it doesn't really change the
				// logic.
				final int minDelay;
				if (Config.chaoticallyPlayMusic) {
					// 10 seconds at 60 FPS
					minDelay = 600;
				} else {
					minDelay = music.getMinDelay();
				}
				this.nextSongDelay = Mth.nextInt(this.random, minDelay, music.getMaxDelay());
			}
		}

		if ((Config.chaoticallyPlayMusic || this.currentMusic == null)
				&& (Config.alwaysPlayMusic || decrementSongDelay(music.getMaxDelay()) <= 0)) {
			if (oldFadingOutMusic != null) {
				this.startPlayingFadeIn(music);
			} else {
				this.startPlaying(music);
			}
		}
	}

	/**
	 * Determines whether to fall back to the previous track despite the situational
	 * music being set to not replace.
	 * <p>
	 * Used to negate 1 block biome switches from replacing the track when the
	 * biome's situational music doesn't replace. Note that the threshold is 75%
	 * volume before the old track is no longer applicable to be swapped back in.
	 *
	 * @param instance      The old instance currently being faded out.
	 * @param musicLocation The music location to test compatibility with.
	 * @return {@code true} if it is not null, loud and compatible, {@code false}
	 *         otherwise.
	 */
	@Unique
	private boolean isLoudAndCompatible(final MusicSoundInstance instance, final ResourceLocation musicLocation) {
		if (instance == null || instance.getDirectVolume() < 0.75F) {
			return false;
		}

		return isCompatible(instance, musicLocation);
	}

	@Unique
	private boolean isReplaceable(final SoundInstance instance, final ResourceLocation musicLocation) {
		return musicLocation != this.currentCompatibleLocation && !isCompatible(instance, musicLocation);
	}

	@Unique
	private boolean isCompatible(final SoundInstance instance, final ResourceLocation musicLocation) {
		if (instance.getLocation().equals(musicLocation)) {
			this.currentCompatibleLocation = musicLocation;
			return true;
		}

		final var weighedSounds = minecraft.getSoundManager().getSoundEvent(musicLocation);

		if (weighedSounds instanceof WeighedSoundEventsQuery query && query.contains(instance.getSound())) {
			this.currentCompatibleLocation = musicLocation;
			return true;
		}

		return false;
	}

	@Unique
	private int decrementSongDelay(int maxDelay) {
		return this.nextSongDelay = Math.min(this.nextSongDelay - 1, maxDelay);
	}

	/**
	 * Reimplementation of {@link MusicManager#startPlaying(Music)} with a fade-in
	 * configured.
	 */
	@Unique
	private void startPlayingFadeIn(Music music) {
		this.currentMusic = new MusicSoundInstance(music.getEvent(), Config.fadeInTicks);
		if (this.currentMusic.getSound() != SoundManager.EMPTY_SOUND) {
			this.minecraft.getSoundManager().play(this.currentMusic);
		}

		this.nextSongDelay = Integer.MAX_VALUE;
	}

	@Redirect(method = "startPlaying", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;forMusic(Lnet/minecraft/sounds/SoundEvent;)Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;"))
	private SimpleSoundInstance musicmoods$simpleSoundInstanceNullifier(final SoundEvent soundEvent) {
		return null;
	}

	@Redirect(method = "startPlaying", at = @At(value = "FIELD", target = "Lnet/minecraft/client/sounds/MusicManager;currentMusic:Lnet/minecraft/client/resources/sounds/SoundInstance;", opcode = Opcodes.PUTFIELD))
	private void musicmoods$setCustomSoundInstance(final MusicManager self, final SoundInstance value,
			final Music music) {
		this.currentMusic = new MusicSoundInstance(music.getEvent());
	}
}
