/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import gay.ampflower.musicmoods.Config;
import gay.ampflower.musicmoods.Constants;
import gay.ampflower.musicmoods.client.MusicHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * @author Ampflower
 * @since 0.6.12
 **/
@Mixin(JukeboxPlayable.class)
public class MixinJukeboxPlayable {
	@Shadow
	@Final
	private EitherHolder<JukeboxSong> song;

	@Inject(method = "addToTooltip", at = @At("RETURN"))
	private void addRightClickToPlay(
		final CallbackInfo ci,
		final @Local(argsOnly = true) Consumer<Component> tooltipConsumer
	) {
		if (!Config.rightClickToPlay) {
			return;
		}

		final var minecraft = Minecraft.getInstance();
		final var level = minecraft.level;

		if (level == null) {
			return;
		}

		final var optionalSong = song.unwrap(level.registryAccess());

		if (optionalSong.isEmpty()) {
			return;
		}

		final var jukeboxSong = optionalSong.get();
		final var stereoSong = jukeboxSong.unwrapKey().flatMap(k -> level.registryAccess().lookup(Registries.JUKEBOX_SONG).orElseThrow().getOptional(Constants.toStereo(k.location())));
		final var musicHandler = (MusicHandler) minecraft.getMusicManager();

		tooltipConsumer.accept(
			musicHandler.moods$isCurrentlyPlaying(stereoSong.orElse(jukeboxSong.value()))
				? Constants.rightClickToStopTooltip
				: Constants.rightClickToPlayTooltip
		);
	}
}
