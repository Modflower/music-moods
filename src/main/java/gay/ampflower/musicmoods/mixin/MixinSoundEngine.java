/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.SoundBuffer;
import gay.ampflower.musicmoods.client.MusicHandler;
import gay.ampflower.musicmoods.client.SoundHandler;
import gay.ampflower.musicmoods.client.sound.MusicSoundInstance;
import gay.ampflower.musicmoods.client.sound.Relativeable;
import gay.ampflower.musicmoods.util.InternalSupport;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

#if MC_1_21_5_OR_OLDER
import gay.ampflower.musicmoods.Config;

import java.util.ArrayList;
#endif

/**
 * @author Ampflower
 * @since 0.5
 **/
@Mixin(SoundEngine.class)
public abstract class MixinSoundEngine implements SoundHandler {
	@Shadow
	@Final
	private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;
	@Shadow
	@Final
	private Map<SoundInstance, Integer> queuedSounds;
	@Shadow
	@Final
	private List<TickableSoundInstance> queuedTickableSounds;

	@Shadow
	private boolean loaded;

	@Shadow
	public abstract void stop(final SoundInstance soundInstance);

	@Shadow
	protected abstract float calculateVolume(final SoundInstance soundInstance);

	@Shadow
	protected abstract float calculatePitch(final SoundInstance soundInstance);

	@ModifyArg(
		method = {
			"tickNonPaused",
			"tickInGameSound",
		},
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V"
		)
	)
	private Consumer<Channel> consumerAudioEquipmentIsQuiteNeatIsntIt(
		Consumer<Channel> original,
		@Local TickableSoundInstance sound
	) {
		if (!(sound instanceof Relativeable relativeable) || !relativeable.isRelativeDirty()) {
			return original;
		}

		final boolean relative = sound.isRelative();
		return channel -> {
			original.accept(channel);
			channel.setRelative(relative);
		};
	}

	#if MC_1_21_5_OR_OLDER

	@Unique
	private final List<TickableSoundInstance> tickingWhilePaused = new ArrayList<>();

	@Inject(method = "tick", at = @At("TAIL"))
	private void tickHook(boolean paused, CallbackInfo ci) {
		if (!paused) {
			return;
		}

		final var iterator = this.tickingWhilePaused.iterator();
		while(iterator.hasNext()) {
			final TickableSoundInstance sound = iterator.next();

			if (!sound.canPlaySound()) {
				this.stop(sound);
				iterator.remove();
				continue;
			}

			sound.tick();

			if (sound.isStopped()) {
				this.stop(sound);
				iterator.remove();
				continue;
			}

			final var handle = this.instanceToChannel.get(sound);

			if (handle == null) {
				continue;
			}

			this.tickSound(sound, handle);
		}
	}

	@Inject(method = "pause", at = @At("HEAD"), cancellable = true)
	private void onPause(CallbackInfo ci) {
		if (Config.allowPausingMusic) {
			return;
		}
		ci.cancel();

		for (final var entry : this.instanceToChannel.entrySet()) {
			if (entry.getKey() instanceof MusicSoundInstance) {
				this.tickingWhilePaused.add((TickableSoundInstance) entry.getKey());
				continue;
			}
			entry.getValue().execute(Channel::pause);
		}
	}

	@Inject(method = "resume", at = @At("HEAD"))
	private void onResume(CallbackInfo ci) {
		this.tickingWhilePaused.clear();
	}

	@Inject(method = "stopAll", at = @At(value = "FIELD", target = "Lnet/minecraft/client/sounds/SoundEngine;tickingSounds:Ljava/util/List;", shift = At.Shift.AFTER))
	private void onStopAll(final CallbackInfo ci) {
		this.tickingWhilePaused.clear();
	}

	#else

	@Inject(method = "tickMusicWhenPaused", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;isStopped()Z"))
	private void moods$tickMusicTicker(
		final CallbackInfo ci, final @Local ChannelAccess.ChannelHandle handle,
		final @Local SoundInstance sound
	) {
		if (!(sound instanceof MusicSoundInstance music)) {
			return;
		}

		music.tick();

		if (music.isStopped()) {
			this.stop(music);
			return;
		}

		tickSound(sound, handle);
	}

	#endif

	@Unique
	private void tickSound(
		final SoundInstance sound,
		final ChannelAccess.ChannelHandle handle
	) {
		final float v = this.calculateVolume(sound);
		final float p = this.calculatePitch(sound);
		final var pos = new Vec3(sound.getX(), sound.getY(), sound.getZ());

		final boolean relativeDirty;
		final boolean relative;

		if (sound instanceof Relativeable relativeable) {
			relativeDirty = relativeable.isRelativeDirty();
			relative = sound.isRelative();
		} else {
			relativeDirty = false;
			relative = false;
		}

		handle.execute(channel -> {
			channel.setVolume(v);
			channel.setPitch(p);
			channel.setSelfPosition(pos);

			if (relativeDirty) {
				channel.setRelative(relative);
			}
		});
	}


	@Inject(
		method = {"tickNonPaused", "tickMusicWhenPaused", "tickInGameSound"},
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/sounds/SoundEngine;instanceBySource:Lcom/google/common/collect/Multimap;"
		)
	)
	private void captureStoppedSoundInstance(final CallbackInfo ci, final @Local SoundInstance instance) {
		MusicHandler.instance.moods$removeProbableIntrusion(instance);
	}

	@Definition(
		id = "thenAccept",
		method = "Ljava/util/concurrent/CompletableFuture;thenAccept(Ljava/util/function/Consumer;)Ljava/util/concurrent/CompletableFuture;"
	)
	@Definition(
		id = "getCompleteBuffer",
		method = {
			"Lnet/minecraft/client/sounds/SoundBufferLibrary;getCompleteBuffer(Lnet/minecraft/resources/Identifier;)Ljava/util/concurrent/CompletableFuture;",
			"Lnet/minecraft/client/sounds/SoundBufferLibrary;getCompleteBuffer(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/concurrent/CompletableFuture;",
		}
	)
	@Expression("@(?.getCompleteBuffer(?)).thenAccept(?)")
	@ModifyExpressionValue(method = "play", at = @At("MIXINEXTRAS:EXPRESSION"))
	private CompletableFuture<SoundBuffer> detectMusicFromCompleteBuffer(
		final CompletableFuture<SoundBuffer> self,
		final @Local(argsOnly = true) SoundInstance instance,
		final @Local SoundSource soundSource
	) {
		// What we really need is a peek.
		return self.thenApply(buf -> {
			final var format = ((AccessorSoundBuffer) buf).format;
			if (format.getChannels() != 1) {
				// Stereo+ cannot be positioned due to engine limitations.
				// It will always be played as if it is positioned relatively at 0, 0, 0.
				MusicHandler.instance.moods$addProbableIntrusion(instance, true);
			} else if (InternalSupport.musicalSources.contains(soundSource)) {
				MusicHandler.instance.moods$addProbableIntrusion(instance, false);
			}

			return buf;
		});
	}

	@Definition(
		id = "thenAccept",
		method = "Ljava/util/concurrent/CompletableFuture;thenAccept(Ljava/util/function/Consumer;)Ljava/util/concurrent/CompletableFuture;"
	)
	@Definition(
		id = "getStream",
		method = {
			"Lnet/minecraft/client/sounds/SoundBufferLibrary;getStream(Lnet/minecraft/resources/Identifier;Z)Ljava/util/concurrent/CompletableFuture;",
			"Lnet/minecraft/client/sounds/SoundBufferLibrary;getStream(Lnet/minecraft/resources/ResourceLocation;Z)Ljava/util/concurrent/CompletableFuture;",
		}
	)
	@Definition(
		id = "youveBeenForged",
		method = "Lnet/minecraft/client/resources/sounds/SoundInstance;getStream(Lnet/minecraft/client/sounds/SoundBufferLibrary;Lnet/minecraft/client/resources/sounds/Sound;Z)Ljava/util/concurrent/CompletableFuture;"
	)
	@Expression(
		{
			"@(?.getStream(?, ?)).thenAccept(?)",
			"@(?.youveBeenForged(?, ?, ?)).thenAccept(?)"
		}
	)
	@ModifyExpressionValue(method = "play", at = @At("MIXINEXTRAS:EXPRESSION"))
	private CompletableFuture<AudioStream> detectMusicFromAudioStream(
		final CompletableFuture<AudioStream> self,
		final @Local(argsOnly = true) SoundInstance instance,
		final @Local SoundSource soundSource
	) {
		// What we really need is a peek.
		return self.thenApply(stream -> {
			if (stream.getFormat().getChannels() != 1) {
				// Stereo+ cannot be positioned due to engine limitations.
				// It will always be played as if it is positioned relatively at 0, 0, 0.
				MusicHandler.instance.moods$addProbableIntrusion(instance, true);
			} else if (InternalSupport.musicalSources.contains(soundSource)) {
				MusicHandler.instance.moods$addProbableIntrusion(instance, false);
			}

			return stream;
		});
	}

	@Override
	public void moods$stopMusic() {
		if (!this.loaded) {
			return;
		}

		for (final var entry : this.instanceToChannel.entrySet()) {
			if (isMusic(entry.getKey())) {
				entry.getValue().execute(Channel::stop);
			}
		}
	}

	@Override
	public void moods$stopSounds() {
		if (!this.loaded) {
			return;
		}

		for (final var entry : this.instanceToChannel.entrySet()) {
			if (isNotMusic(entry.getKey())) {
				entry.getValue().execute(Channel::stop);
			}
		}
	}

	@Override
	public void moods$fadeSounds(final float ticks) {
		if (!this.loaded) {
			return;
		}

		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void moods$clearQueued() {
		this.queuedSounds.clear();
		this.queuedTickableSounds.clear();
	}

	@Unique
	private static boolean isMusic(SoundInstance instance) {
		return instance instanceof MusicSoundInstance;
	}

	@Unique
	private static boolean isNotMusic(SoundInstance instance) {
		return !(instance instanceof MusicSoundInstance);
	}
}
