/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;

import gay.ampflower.musicmoods.Config;
import gay.ampflower.musicmoods.client.sound.Fadeable;
import gay.ampflower.musicmoods.client.sound.RecordSoundInstance;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Ampflower
 * @since 0.5
 **/
@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

	@Redirect(method = "playStreamingMusic", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;stop(Lnet/minecraft/client/resources/sounds/SoundInstance;)V"))
	private static void fadeOutRecord(final SoundManager manager, final SoundInstance instance) {
		if (Config.jukeboxEnabled && Config.jukeboxFadeStopTicks > 0 && instance instanceof Fadeable fadeable) {
			fadeable.setFadeOut(Config.jukeboxFadeStopTicks);
		} else {
			manager.stop(instance);
		}
	}

	@Redirect(method = "playStreamingMusic", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;forRecord(Lnet/minecraft/sounds/SoundEvent;DDD)Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;"))
	private static SimpleSoundInstance nullifyRecord(final SoundEvent soundEvent, double x, double y, double z) {
		return null;
	}

	@ModifyVariable(method = "playStreamingMusic", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;forRecord(Lnet/minecraft/sounds/SoundEvent;DDD)Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;", shift = At.Shift.AFTER))
	private SoundInstance createRecordInstance(final SoundInstance instance, final SoundEvent soundEvent,
			final BlockPos blockPos) {
		return new RecordSoundInstance(soundEvent, blockPos);
	}
}
