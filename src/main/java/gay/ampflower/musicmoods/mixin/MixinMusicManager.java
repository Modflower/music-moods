/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;// Created 2022-24-12T20:34:50

import gay.ampflower.musicmoods.Config;
import gay.ampflower.musicmoods.Mint;
import gay.ampflower.musicmoods.client.WeighedSoundEventsQuery;
import gay.ampflower.musicmoods.client.sound.MusicSoundInstance;
import gay.ampflower.musicmoods.client.sound.RecordSoundInstance;
import gay.ampflower.musicmoods.config.Replacing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicInfo;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Ampflower
 * @since 0.0.0
 **/
@Mixin(value = MusicManager.class, priority = 500)
public abstract class MixinMusicManager {
	@Shadow
	@Nullable
	private SoundInstance currentMusic;
	@Shadow
	private MusicManager.MusicFrequency gameMusicFrequency;
	@Shadow
	private float currentGain;

	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	private int nextSongDelay;
	@Shadow
	@Final
	private RandomSource random;

	@Shadow
	public abstract void startPlaying(final MusicInfo music);

	@Shadow
	private boolean fadePlaying(final float volume) {
		throw new AssertionError();
	}

	@Unique
	private RecordSoundInstance focusedJukebox;
	@Unique
	private MusicSoundInstance fadingOutMusic;
	@Unique
	private ResourceLocation currentCompatibleLocation;

	/**
	 * @author Ampflower
	 * @reason Better integration without having injects and redirects everywhere.
	 */
	@Overwrite
	public void tick() {
		if (Config.jukeboxEnabled
				&& (!Config.jukeboxMultiplayer || !((AccessorMinecraft) minecraft).invokeIsMultiplayerServer())) {
			handleRecords();
		}

		final var musicInfo = this.minecraft.getSituationalMusic();
		final var music = musicInfo.music();
		final var soundManager = this.minecraft.getSoundManager();

		if (this.currentMusic != null) {
			final float volume = musicInfo.volume();

			if (this.currentGain != volume) {
				// note: return intentionally not replicated.
				boolean playing = this.fadePlaying(volume);
				if (!playing) {
					this.stopMusic();
				}
			}

			// Seamless Transitions requires this to be before the music == null check.
			if (!soundManager.isActive(this.currentMusic)) {
				this.clearCurrent();

				this.nextSongDelay = this.deriveNextSongDelay(music);
			}
		}

		var fadingOutMusic = this.fadingOutMusic;

		if (fadingOutMusic != null && !soundManager.isActive(fadingOutMusic)) {
			this.fadingOutMusic = fadingOutMusic = null;
		}

		// Vanilla behaviour.
		if (music == null) {
			this.handleMissingTrack();
			return;
		}

		final var musicLocation = music.event().value().location();

		// Allow the end user to say whether they want their music replaced at all.
		if (this.currentMusic != null && Config.situationalMusicReplacing.replaces()
				&& (isLoudAndCompatible(fadingOutMusic, musicLocation) || shouldReplace(music))) {
			this.fadeOrStopMusic();

			if (isCompatible(fadingOutMusic, musicLocation)) {
				this.currentMusic = fadingOutMusic;
				fadingOutMusic.setFadeIn(Config.fadeInTicks);
			} else if (Config.immediatelyPlayOnReplace) {
				this.startPlayingFadeIn(music);
			} else {
				// Clear currentMusic, so it's not trying to tick it.
				this.clearCurrent();

				// Set the delay, since the old track is not applicable to move in.
				this.nextSongDelay = Mth.nextInt(this.random, 0, music.minDelay() / 2);
			}
		}

		if (this.focusedJukebox != null) {
			if (soundManager.isActive(this.focusedJukebox)) {
				return;
			}
			this.focusedJukebox = null;
		}

		if ((Config.chaoticallyPlayMusic || this.currentMusic == null) && (decrementSongDelay(music) <= 0)) {
			if (fadingOutMusic != null) {
				this.startPlayingFadeIn(music);
			} else {
				this.startPlaying(musicInfo);
			}
		}
	}

	@Unique
	private int deriveNextSongDelay(final Music music) {
		if (music == null) {
			return 100;
		}

		final var frequency = (AccessorMusicFrequency) (Object) this.gameMusicFrequency;
		if (frequency != null) {
			return Math.min(this.nextSongDelay, frequency.invokeGetNextSongDelay(music, this.random));
		}

		// This shouldn't happen but IntelliJ is complaining.
		// Fall back to legacy logic in case this is actually true.

		final int minDelay;
		if (Config.chaoticallyPlayMusic) {
			// 10 seconds at 60 FPS
			minDelay = 600;
		} else {
			minDelay = music.minDelay();
		}

		return Math.min(this.nextSongDelay, Mth.nextInt(this.random, minDelay, music.maxDelay()));
	}

	@Unique
	private void handleMissingTrack() {
		this.nextSongDelay = Math.max(this.nextSongDelay, 100);

		// Slightly deviant from vanilla behaviour; tho the biome may be requesting
		// silence.
		if (Config.situationalMusicReplacing == Replacing.always) {
			this.fadeOrStopMusic();
		}
	}

	@Unique
	private void handleRecords() {
		final var player = this.minecraft.player;
		if (player == null) {
			return;
		}

		final float fadeSq = Mint.square(Config.jukeboxFadeRange);
		final float replSq = Mint.square(Config.jukeboxReplaceRange);
		final float maxSq = Math.max(fadeSq, replSq);

		final var camera = this.minecraft.gameRenderer.getMainCamera();

		final var cameraPos = camera.getPosition();
		final var cameraRot = Mint.cameraToRotationVector(camera);

		if (maxSq <= 0) {
			if (this.focusedJukebox != null) {
				this.focusedJukebox.centerOnOrigin(cameraPos, cameraRot);
				this.focusedJukebox = null;
			}
			return;
		}

		final var levelEventHandler = ((AccessorClientLevel) this.minecraft.level).getLevelEventHandler();

		final var map = ((AccessorLevelEventHandler) levelEventHandler).getPlayingJukeboxSongs();
		if (map.isEmpty() || map.size() > 128) {
			return;
		}

		final var soundManager = this.minecraft.getSoundManager();

		final var itr = map.entrySet().iterator();
		RecordSoundInstance lastRecord = null;
		double lastDelta = Double.POSITIVE_INFINITY;
		while (itr.hasNext()) {
			final var entry = itr.next();

			if (!soundManager.isActive(entry.getValue())) {
				itr.remove();
				continue;
			}

			if (!(entry.getValue()instanceof RecordSoundInstance record)) {
				continue;
			}

			var delta = entry.getKey().distToCenterSqr(player.getEyePosition());

			if (delta > maxSq) {
				continue;
			}

			if (delta < lastDelta) {
				lastRecord = record;
				lastDelta = delta;
			}
		}

		if (this.focusedJukebox != null && this.focusedJukebox != lastRecord) {
			this.focusedJukebox.centerOnOrigin(cameraPos, cameraRot);
		}
		if (lastRecord != null && lastDelta > replSq) {
			lastRecord.centerOnOrigin(cameraPos, cameraRot);
		}
		this.focusedJukebox = lastRecord;

		if (lastRecord == null) {
			return;
		}

		if (lastDelta < replSq) {
			lastRecord.centerOnPlayer(cameraPos, cameraRot);
		}

		if (this.currentMusic instanceof MusicSoundInstance music) {
			music.setFadeOut(Config.jukeboxFadeMixTicks);
			fadingOutMusic = music;
		} else {
			soundManager.stop(this.currentMusic);
		}

		this.currentMusic = null;
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
	private boolean shouldReplace(final Music music) {
		return (Config.situationalMusicReplacing == Replacing.always || music.replaceCurrentMusic())
				&& this.isReplaceable(this.currentMusic, music.event().value().location());
	}

	@Unique
	private boolean isReplaceable(final SoundInstance instance, final ResourceLocation musicLocation) {
		return musicLocation != this.currentCompatibleLocation && !isCompatible(instance, musicLocation);
	}

	@Unique
	private boolean isCompatible(final SoundInstance instance, final ResourceLocation musicLocation) {
		if (instance == null || musicLocation == null) {
			return false;
		}

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
	private int decrementSongDelay(Music music) {
		final var frequency = (AccessorMusicFrequency) (Object) this.gameMusicFrequency;
		final int maxDelay;
		if (frequency == null) {
			maxDelay = music.maxDelay();
		} else {
			maxDelay = Math.min(music.maxDelay(), frequency.getMaxFrequency());
		}
		return this.nextSongDelay = Math.min(this.nextSongDelay - 1, maxDelay);
	}

	/**
	 * Reimplementation of {@link MusicManager#startPlaying(MusicInfo)} with a
	 * fade-in configured.
	 */
	@Unique
	private void startPlayingFadeIn(Music music) {
		this.currentMusic = new MusicSoundInstance(music.event().value(), Config.fadeInTicks);
		if (this.currentMusic.getSound() != SoundManager.EMPTY_SOUND) {
			this.minecraft.getSoundManager().play(this.currentMusic);
		}

		this.nextSongDelay = Integer.MAX_VALUE;
	}

	@Unique
	private void fadeOrStopMusic() {
		// Do a fade out on the current music if configured to make it not jarring.
		if (Config.fadeOutTicks > 0 && this.currentMusic instanceof MusicSoundInstance musicSoundInstance) {
			musicSoundInstance.setFadeOut(Config.fadeOutTicks);
			this.fadingOutMusic = musicSoundInstance;
			this.clearCurrent();
		} else {
			this.stopMusic();
		}
	}

	@Unique
	private void stopMusic() {
		this.minecraft.getSoundManager().stop(this.currentMusic);
		this.clearCurrent();
	}

	@Unique
	private void clearCurrent() {
		this.currentMusic = null;
		this.currentCompatibleLocation = null;
	}

	@Redirect(method = "startPlaying", at = @At(value = "INVOKE", target = "net/minecraft/client/resources/sounds/SimpleSoundInstance.forMusic (Lnet/minecraft/sounds/SoundEvent;F)Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;"))
	private SimpleSoundInstance musicmoods$simpleSoundInstanceNullifier(final SoundEvent soundEvent, float volume) {
		return null;
	}

	@Redirect(method = "startPlaying", at = @At(value = "FIELD", target = "Lnet/minecraft/client/sounds/MusicManager;currentMusic:Lnet/minecraft/client/resources/sounds/SoundInstance;", opcode = Opcodes.PUTFIELD))
	private void musicmoods$setCustomSoundInstance(MusicManager instance, SoundInstance value, MusicInfo musicInfo) {
		this.currentMusic = new MusicSoundInstance(musicInfo.music().event().value(), musicInfo.volume());
	}
}
