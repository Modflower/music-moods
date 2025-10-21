/*
 * Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package gay.ampflower.musicmoods.mixin;

#if MC_1_20_5_OR_OLDER

import com.llamalad7.mixinextras.sugar.Local;
import gay.ampflower.musicmoods.Sounds;
import gay.ampflower.musicmoods.util.JukeboxUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.RecordItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

/**
 * @author Ampflower
 * @since 0.7
 **/
@Mixin(RecordItem.class)
public class MixinRecordItem {
	@Shadow
	private SoundEvent getSound() {
		throw new AssertionError();
	}

	@Inject(method = "appendHoverText", at = @At("RETURN"))
	private void musicmoods$rightClickToPlay(
		final CallbackInfo ci,
		final @Local(argsOnly = true) List<Component> list
	) {
		JukeboxUtil.appendRightClickToPlay(list::add, access -> Optional.ofNullable(Sounds.findStereo(sound)));
	}
}

#endif
