/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;

import gay.ampflower.musicmoods.Config;
import gay.ampflower.musicmoods.client.sound.Fadeable;
import gay.ampflower.musicmoods.client.sound.RecordSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

#if MC_1_21_OR_NEWER
import net.minecraft.core.Holder;
import net.minecraft.world.item.JukeboxSong;
#endif

#if MC_1_21_2_OR_NEWER
import net.minecraft.client.renderer.LevelEventHandler;
#else
import net.minecraft.client.renderer.LevelRenderer;
#endif

/**
 * @author Ampflower
 * @since 0.5
 **/
@Mixin(#if (MC_1_21_2_OR_NEWER) LevelEventHandler.class #else LevelRenderer.class #endif )
public class MixinLevelRenderer {

	#if MC_1_20_5_OR_OLDER

	@Redirect(method = "playStreamingMusic", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;stop(Lnet/minecraft/client/resources/sounds/SoundInstance;)V"))
	private void fadeOutRecord(final SoundManager manager, final SoundInstance instance) {
		if (Config.jukeboxEnabled && Config.jukeboxFadeStopTicks > 0 && instance instanceof Fadeable fadeable) {
			fadeable.setFadeOut(Config.jukeboxFadeStopTicks);
		} else {
			manager.stop(instance);
		}
	}

	#if MC_1_19_OR_OLDER

	@Redirect(method = "playStreamingMusic", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;forRecord(Lnet/minecraft/sounds/SoundEvent;DDD)Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;"))
	private SimpleSoundInstance nullifyRecord(
		final SoundEvent soundEvent,
		final double x,
		final double y,
		final double z
	) {
		return null;
	}

	@ModifyVariable(method = "playStreamingMusic", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;forRecord(Lnet/minecraft/sounds/SoundEvent;DDD)Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;", shift = At.Shift.AFTER))
	private SoundInstance createRecordInstance(
		final SoundInstance instance,
		final SoundEvent soundEvent,
		final BlockPos blockPos
	) {
		return new RecordSoundInstance(soundEvent, blockPos);
	}
	#else
	@Redirect(method = "playStreamingMusic", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;forRecord(Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;"))
	private SimpleSoundInstance nullifyRecord(final SoundEvent soundEvent, final Vec3 vec3) {
		return null;
	}

	@ModifyVariable(method = "playStreamingMusic", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;forRecord(Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;", shift = At.Shift.AFTER))
	private SoundInstance createRecordInstance(final SoundInstance instance, final SoundEvent soundEvent,
											   final BlockPos blockPos) {
		return new RecordSoundInstance(soundEvent, blockPos);
	}
	#endif

	#else

	@Redirect(method = "stopJukeboxSong", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;stop(Lnet/minecraft/client/resources/sounds/SoundInstance;)V"))
	private void fadeOutRecord(final SoundManager manager, final SoundInstance instance) {
		if (Config.jukeboxEnabled && Config.jukeboxFadeStopTicks > 0 && instance instanceof Fadeable fadeable) {
			fadeable.setFadeOut(Config.jukeboxFadeStopTicks);
		} else {
			manager.stop(instance);
		}
	}

	@Redirect(method = "playJukeboxSong", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;forJukeboxSong(Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;"))
	private SimpleSoundInstance nullifyRecord(final SoundEvent soundEvent, final Vec3 pos) {
		return null;
	}

	@ModifyVariable(method = "playJukeboxSong", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;forJukeboxSong(Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;", shift = At.Shift.AFTER))
	private SoundInstance createRecordInstance(final SoundInstance instance, final Holder<JukeboxSong> jukeboxSong,
			final BlockPos blockPos) {
		return new RecordSoundInstance(jukeboxSong.value().soundEvent().value(), blockPos);
	}
	#endif
}
