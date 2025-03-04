/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Consumer;

/**
 * @author Ampflower
 * @since 0.5
 **/
@Mixin(SoundEngine.class)
public class MixinSoundEngine {
	@ModifyArg(method = "tickNonPaused", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V"))
	private Consumer<Channel> consumerAudioEquipmentIsQuiteNeatIsntIt(Consumer<Channel> original,
			@Local TickableSoundInstance sound) {
		return channel -> {
			original.accept(channel);
			channel.setRelative(sound.isRelative());
		};
	}
}
