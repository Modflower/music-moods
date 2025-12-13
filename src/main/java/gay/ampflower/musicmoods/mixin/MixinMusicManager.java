/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;// Created 2022-24-12T20:34:50

import gay.ampflower.musicmoods.Config;
import gay.ampflower.musicmoods.Mint;
import gay.ampflower.musicmoods.client.MusicHandler;
import gay.ampflower.musicmoods.client.WeighedSoundEventsQuery;
import gay.ampflower.musicmoods.client.sound.MusicSoundInstance;
import gay.ampflower.musicmoods.client.sound.RecordSoundInstance;
import gay.ampflower.musicmoods.config.Replacing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
#if MC_1_21_4_OR_NEWER && !MC_1_21_11_OR_NEWER
#define MUSIC_INFO
import net.minecraft.client.sounds.MusicInfo;
#endif
import net.minecraft.client.sounds.MusicManager;
#if MC_1_21_6_OR_NEWER
import net.minecraft.client.sounds.SoundEngine;
#endif
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
#if MC_1_21_11_OR_NEWER
import net.minecraft.resources.Identifier;
#else
import net.minecraft.resources.ResourceLocation;
#endif
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Ampflower
 * @since 0.0.0
 **/
@Mixin(value = MusicManager.class, priority = 500)
public abstract class MixinMusicManager implements MusicHandler {
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

	#if MC_1_21_4_OR_NEWER
	@Shadow
	private float currentGain;

	@Shadow
	private boolean fadePlaying(final float volume) {
		throw new AssertionError();
	}
	#endif

	#if MC_1_21_6_OR_NEWER
	@Shadow
	private MusicManager.MusicFrequency gameMusicFrequency;
	@Shadow
	private boolean toastShown;

	@Shadow
	private String getCurrentMusicTranslationKey() {
		throw new AssertionError();
	}

	/**
	 * Records don't really bodge in well...
	 */
	@Unique
	private Component currentMusicName;
	#endif

	/**
	 * Whether the current track is interruptable.
	 */
	@Unique
	private boolean currentMusicIntruded;
	@Unique
	private RecordSoundInstance focusedJukebox;
	@Unique
	private MusicSoundInstance fadingOutMusic;
	#if MC_1_21_11_OR_NEWER
	@Unique
	private Identifier currentCompatibleLocation;
	#else
	@Unique
	private ResourceLocation currentCompatibleLocation;
	#endif

	/**
	 * @author Ampflower
	 * @reason Better integration without having injects and redirects everywhere.
	 */
	@Overwrite
	public void tick() {
		if (Config.jukeboxEnabled
			&& (Config.jukeboxMultiplayer || !((AccessorMinecraft) minecraft).invokeIsMultiplayerServer())) {
			handleRecords();
		}

		final var musicInfo = this.minecraft.getSituationalMusic();
		//noinspection UnnecessaryLocalVariable - actually is used
		final var music = #if (MUSIC_INFO) musicInfo.music() #else musicInfo #endif ;
		final var soundManager = this.minecraft.getSoundManager();

		if (this.currentMusic != null) {
			#if MC_1_21_4_OR_NEWER
			final float volume = #if (MUSIC_INFO) musicInfo.volume() #else minecraft.musicVolume #endif;

			if (this.currentGain != volume) {
				// note: return intentionally not replicated.
				boolean playing = this.fadePlaying(volume);
				if (!playing) {
					this.stopMusic();
				}
			}
			#endif

			// Seamless Transitions requires this to be before the music == null check.
			if (!soundManager.isActive(this.currentMusic)) {
				this.clearCurrent();

				this.nextSongDelay = this.deriveNextSongDelay(music);
			}
			#if MC_1_21_6_OR_NEWER
			else if (!this.toastShown && this.currentMusic.getVolume() >= 0.75F) {
				this.minecraft.getToastManager().showNowPlayingToast();
				this.toastShown = true;
			}
			#endif
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

		final var musicLocation = getLocation(music);

		// Allow the end user to say whether they want their music replaced at all.
		if (this.currentMusic != null && !this.currentMusicIntruded && Config.situationalMusicReplacing.replaces()
				&& (isLoudAndCompatible(fadingOutMusic, musicLocation) || shouldReplace(music))) {
			this.fadeOrStopMusic();

			if (isCompatible(fadingOutMusic, musicLocation)) {
				this.reset(0);
				this.currentMusic = fadingOutMusic;
				fadingOutMusic.setFadeIn(Config.fadeInTicks);
			} else if (Config.immediatelyPlayOnReplace) {
				this.startPlayingFadeIn(musicInfo);
			} else {
				// Clear currentMusic, so it's not trying to tick it.
				this.clearCurrent();

				// Set the delay, since the old track is not applicable to move in.
				this.nextSongDelay = Mth.nextInt(this.random, 0, music.minDelay / 2);
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
				this.startPlayingFadeIn(musicInfo);
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

		#if MC_1_21_6_OR_NEWER
		final var frequency = (AccessorMusicFrequency) (Object) this.gameMusicFrequency;
		if (frequency != null) {
			return Math.min(this.nextSongDelay, frequency.invokeGetNextSongDelay(music, this.random));
		}
		#endif

		// This shouldn't happen but IntelliJ is complaining.
		// Fall back to legacy logic in case this is actually true.

		final int minDelay;
		#if MC_1_21_5_OR_OLDER
		if (Config.alwaysPlayMusic) {
			minDelay = 100;
		} else
		#endif
		if (Config.chaoticallyPlayMusic) {
			// 10 seconds at 60 FPS
			minDelay = 600;
		} else {
			minDelay = music.minDelay;
		}

		return Math.min(this.nextSongDelay, Mth.nextInt(this.random, minDelay, music.maxDelay));
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
		// Don't override intruded tracks.
		if (this.currentMusicIntruded) {
			return;
		}

		final var player = this.minecraft.player;
		if (player == null) {
			return;
		}

		final float fadeSq = Mint.square(Config.jukeboxFadeRange);
		final float replSq = Mint.square(Config.jukeboxReplaceRange);
		final float maxSq = Math.max(fadeSq, replSq);

		final var camera = this.minecraft.gameRenderer.getMainCamera();

		final var cameraPos = #if (MC_1_21_11_OR_NEWER) camera.position() #else camera.getPosition() #endif ;
		final var cameraRot = Mint.cameraToRotationVector(camera);

		if (maxSq <= 0) {
			if (this.focusedJukebox != null) {
				this.focusedJukebox.centerOnOrigin(cameraPos, cameraRot);
				this.focusedJukebox = null;
			}
			return;
		}

		#if MC_1_21_OR_OLDER
		final var levelEventHandler = this.minecraft.levelRenderer;
		#else
		final var levelEventHandler = ((AccessorClientLevel) this.minecraft.level).getLevelEventHandler();
		#endif

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

		this.fadeOrStopMusic(Config.jukeboxFadeMixTicks);
	}

	@Override
	public boolean moods$intrudeJukeboxTrack(
		final @NotNull Holder<SoundEvent> soundEvent,
		final @Nullable Component name
	) {
		if (
			this.currentMusicIntruded &&
			this.isCompatible(this.currentMusic, soundEvent.value().location)
		) {
			return false;
		}

		this.startPlayingIntruded(soundEvent, name);

		return true;
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
	private boolean isLoudAndCompatible(
		final MusicSoundInstance instance,
		final #if (MC_1_21_11_OR_NEWER) Identifier #else ResourceLocation #endif musicLocation
	) {
		if (instance == null || instance.getDirectVolume() < 0.75F) {
			return false;
		}

		return isCompatible(instance, musicLocation);
	}

	@Unique
	private boolean shouldReplace(final Music music) {
		return (Config.situationalMusicReplacing == Replacing.always || music.replaceCurrentMusic())
			   && this.isReplaceable(this.currentMusic, getLocation(music));
	}

	@Unique
	private boolean isReplaceable(
		final SoundInstance instance,
		final #if (MC_1_21_11_OR_NEWER) Identifier #else ResourceLocation #endif musicLocation
	) {
		return musicLocation != this.currentCompatibleLocation && !isCompatible(instance, musicLocation);
	}

	@Override
	public boolean moods$isCurrentlyPlaying(final SoundEvent soundEvent) {
		if (!this.currentMusicIntruded) {
			return false;
		}

		return isCompatible(this.currentMusic, soundEvent.location);
	}

	@Unique
	private boolean isCompatible(
		final SoundInstance instance,
		final #if (MC_1_21_11_OR_NEWER) Identifier #else ResourceLocation #endif musicLocation
	) {
		if (instance == null || musicLocation == null) {
			return false;
		}

		final var instLocation = #if (MC_1_21_11_OR_NEWER) instance.identifier #else instance.location #endif ;

		if (instLocation.equals(musicLocation)) {
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
		int maxDelay = music.maxDelay;
		#if MC_1_21_6_OR_NEWER
		final var frequency = (AccessorMusicFrequency) (Object) this.gameMusicFrequency;
		if (frequency != null) {
			maxDelay = Math.min(maxDelay, frequency.getMaxFrequency());
		}
		#else
		if (Config.alwaysPlayMusic) {
			maxDelay = 100;
		}
		#endif
		return this.nextSongDelay = Math.min(this.nextSongDelay - 1, maxDelay);
	}

	#if !MUSIC_INFO
	/**
	 * Reimplementation of {@link MusicManager#startPlaying(Music)} with a
	 * fade-in configured.
	 */
	@Unique
	private void startPlayingFadeIn(final Music music) {
		if (music == null) {
			return;
		}

		this.startPlayingCommon(#if MC_1_21_11_OR_NEWER music.sound #else music.event #endif , null, 1.f, Config.fadeInTicks);
	}
	#else

	/**
	 * Reimplementation of {@link MusicManager#startPlaying(MusicInfo)} with a
	 * fade-in configured.
	 */
	@Unique
	private void startPlayingFadeIn(final MusicInfo music) {
		if (music.music() == null || music.volume() <= 0) {
			return;
		}

		this.startPlayingCommon(music.music.event, null, music.volume(), Config.fadeInTicks);
	}
	#endif

	/**
	 * @author Ampflower
	 * @reason The original logic is proving itself ill-suited for Music Moods' needs
	 */
	#if !MUSIC_INFO
	@Overwrite
	public void startPlaying(final Music music) {
		if (music == null) {
			return;
		}

		this.startPlayingCommon(#if MC_1_21_11_OR_NEWER music.sound #else music.event #endif , null, 1.f, 0);
	}
	#else
	@Overwrite
	public void startPlaying(final MusicInfo music) {
		if (music.music() == null || music.volume() <= 0) {
			return;
		}

		this.startPlayingCommon(music.music.event, null, music.volume(), 0);
	}
	#endif

	@Unique
	private void startPlayingIntruded(final Holder<SoundEvent> soundEventHolder, final Component name) {
		this.reset(Config.jukeboxFadeMixTicks);

		this.currentMusicIntruded = true;
		this.startPlayingCommon(soundEventHolder, name, 1.f, 0);
	}

	@Unique
	private void startPlayingCommon(
		final Holder<SoundEvent> soundEvent,
		@SuppressWarnings("unused") final @Nullable Component name,
		@SuppressWarnings({"unused", "SameParameterValue"}) final float volume,
		final float fadeInTicks
	) {
		this.startPlayingCommon(soundEvent.value(), name, volume, fadeInTicks);
	}

	@Unique
	private void startPlayingCommon(
		final SoundEvent soundEvent,
		@SuppressWarnings("unused") final @Nullable Component name,
		@SuppressWarnings({"unused", "SameParameterValue"}) final float volume,
		final float fadeInTicks
	) {
		this.currentMusic = new MusicSoundInstance(soundEvent, fadeInTicks);
		this.nextSongDelay = Integer.MAX_VALUE;
		#if MC_1_21_4_OR_NEWER
		this.currentGain = volume;
		#endif

		if (this.currentMusic.getSound() == SoundManager.EMPTY_SOUND) {
			return;
		}

		#if MC_1_21_5_OR_OLDER
		this.minecraft.getSoundManager().play(this.currentMusic);
		#else
		final var state = this.minecraft.getSoundManager().play(this.currentMusic);
		if (state == SoundEngine.PlayResult.NOT_STARTED) {
			return;
		}

		this.updateCurrentMusicName(name);

		if (state == SoundEngine.PlayResult.STARTED || fadeInTicks <= 0) {
			this.minecraft.getToastManager().showNowPlayingToast();
			this.toastShown = true;
		} else {
			this.toastShown = false;
		}
		#endif
	}

	@Unique
	private void reset(final int fadeOut) {
		// Reset jukebox
		final var camera = this.minecraft.gameRenderer.getMainCamera();

		final var cameraPos = #if (MC_1_21_11_OR_NEWER) camera.position() #else camera.getPosition() #endif ;
		final var cameraRot = Mint.cameraToRotationVector(camera);

		if (this.focusedJukebox != null) {
			this.focusedJukebox.centerOnOrigin(cameraPos, cameraRot);
			this.focusedJukebox = null;
		}

		this.fadeOrStopMusic(fadeOut);
	}

	@Unique
	private void fadeOrStopMusic() {
		this.fadeOrStopMusic(Config.fadeOutTicks);
	}

	@Unique
	private void fadeOrStopMusic(final float fadeOut) {
		// Do a fade out on the current music if configured to make it not jarring.
		if (fadeOut > 0 && this.currentMusic instanceof MusicSoundInstance music) {
			music.setFadeOut(fadeOut);
			this.fadingOutMusic = music;
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
		this.currentMusicIntruded = false;
		this.currentCompatibleLocation = null;

		#if MC_1_21_6_OR_NEWER
		this.currentMusicName = null;
		this.toastShown = false;
		this.minecraft.getToastManager().hideNowPlayingToast();
		#endif
	}

	@Inject(method = {"stopPlaying()V"}, at = @At("RETURN"))
	private void clearOnStopPlaying(CallbackInfo ci) {
		this.clearCurrent();
	}

	#if MC_1_21_6_OR_NEWER
	@Unique
	private void updateCurrentMusicName(final @Nullable Component name) {
		if (this.currentMusic == null) {
			this.currentMusicName = null;
			return;
		}

		if (name != null) {
			this.currentMusicName = name;
			return;
		}

		final String key = this.getCurrentMusicTranslationKey();

		if (key == null) {
			this.currentMusicName = null;
			return;
		}

		this.currentMusicName = Component.translatable(key.replace('/', '.'));
	}

	@Override
	public Component moods$getCurrentMusicName() {
		if (this.currentMusic == null) {
			return Component.empty();
		}

		if (this.currentMusicName == null) {
			updateCurrentMusicName(null);
		}

		return this.currentMusicName;
	}
	#endif

	/**
	 * Gets the identifier of the given music instance.
	 */
	#if MC_1_21_11_OR_NEWER
	private static Identifier getLocation(Music music) {
		return music.sound.value().location();
	}
	#else
	private static ResourceLocation getLocation(Music music) {
		#if MC_1_19_OR_OLDER
		return music.event.location;
		#else
		return music.event.value().location;
		#endif
	}
	#endif
}
