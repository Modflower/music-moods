/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import gay.ampflower.musicmoods.client.MusicHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.NowPlayingToast;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author Ampflower
 * @since ${version}
 **/
@Mixin(NowPlayingToast.class)
public class MixinNowPlayingToast {

	@WrapMethod(method = "getNowPlayingString")
	private static Component cachingNowPlayingString(
		final @Nullable String translation,
		final Operation<Component> originalOperation
	) {
		final var name = ((MusicHandler)Minecraft.getInstance().getMusicManager()).moods$getCurrentMusicName();

		if (name != null) {
			return name;
		}

		return originalOperation.call(translation);
	}

}
