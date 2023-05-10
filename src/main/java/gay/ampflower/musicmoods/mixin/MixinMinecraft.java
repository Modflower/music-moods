/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;// Created 2023-17-01T21:38:15

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * @author Ampflower
 * @since 0.1.0
 **/
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
	@Shadow
	@Nullable
	public LocalPlayer player;

	/**
	 * Fixes underwater music constantly playing when set to always playing or
	 * replacing.
	 *
	 * @reason {@link LocalPlayer#isUnderWater()} check needs to happen as we have a
	 *         continuous mode, in which Minecraft does not account for when telling
	 *         if it should return underwater music.
	 * @since 0.3.0
	 */
	@Redirect(method = "getSituationalMusic", at = @At(value = "INVOKE", ordinal = 0, slice = "underWaterMusicManager", target = "Lnet/minecraft/client/sounds/MusicManager;isPlayingMusic(Lnet/minecraft/sounds/Music;)Z"), slice = @Slice(id = "underWaterMusicManager", from = @At(value = "FIELD", target = "Lnet/minecraft/sounds/Musics;UNDER_WATER:Lnet/minecraft/sounds/Music;")))
	private boolean musicmoods$checkPlayer(MusicManager self, Music music) {
		assert this.player != null : "Minecraft moved underwater check?";
		return this.player.isUnderWater() && self.isPlayingMusic(music);
	}
}
