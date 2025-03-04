/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.audio.Channel;
import gay.ampflower.musicmoods.Config;
import gay.ampflower.musicmoods.client.sound.MusicSoundInstance;
import gay.ampflower.musicmoods.client.sound.Relativeable;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ampflower
 * @since 0.5
 **/
@Mixin(SoundEngine.class)
public abstract class MixinSoundEngine {
	@Shadow
	@Final
	private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

	@Shadow
	public abstract void stop(final SoundInstance soundInstance);

	@Shadow
	protected abstract float calculateVolume(final SoundInstance soundInstance);

	@Shadow
	protected abstract float calculatePitch(final SoundInstance soundInstance);

	@Unique
	private final List<TickableSoundInstance> tickingWhilePaused = new ArrayList<>();

	@ModifyArg(method = "tickNonPaused", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V"))
	private Consumer<Channel> consumerAudioEquipmentIsQuiteNeatIsntIt(Consumer<Channel> original,
			@Local TickableSoundInstance sound) {
		if (!(sound instanceof Relativeable relativeable) || !relativeable.isRelativeDirty()) {
			return original;
		}

		final boolean relative = sound.isRelative();
		return channel -> {
			original.accept(channel);
			channel.setRelative(relative);
		};
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void tickHook(boolean paused, CallbackInfo ci) {
		if (paused) {
			tickPaused();
		}
	}

	@Unique
	private void tickPaused() {
		final var iterator = this.tickingWhilePaused.iterator();
		while (iterator.hasNext()) {
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
}
