/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import gay.ampflower.musicmoods.Config;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * @author Ampflower
 * @since 0.5
 **/
@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
	@WrapWithCondition(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/MusicManager;stopPlaying()V"))
	private boolean musicmoods$dontStopMusic(MusicManager manager) {
		return !Config.seamlessTransitions;
	}
}
