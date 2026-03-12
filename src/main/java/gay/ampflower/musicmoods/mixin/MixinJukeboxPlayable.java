/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;

#if MC_1_21_OR_NEWER

import com.llamalad7.mixinextras.sugar.Local;
import gay.ampflower.musicmoods.util.JukeboxUtil;
import net.minecraft.network.chat.Component;
#if MC_1_21_11_OR_OLDER
import net.minecraft.world.item.EitherHolder;
#else
import net.minecraft.core.Holder;
#endif
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
	#if MC_1_21_11_OR_OLDER
	private EitherHolder<JukeboxSong> song;
	#else
	private Holder<JukeboxSong> song;
	#endif

	@Inject(method = "addToTooltip", at = @At("RETURN"))
	private void addRightClickToPlay(
		final CallbackInfo ci,
		final @Local(argsOnly = true) Consumer<Component> tooltipConsumer
	) {
		JukeboxUtil.appendRightClickToPlay(tooltipConsumer, access -> JukeboxUtil.toStereoElseMono(access, song));
	}
}

#endif
