/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;// Created 2023-17-01T21:38:15

import gay.ampflower.musicmoods.client.WidgetAttachment;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Late-init hook because Quilt hooks in too early for what we need.
 *
 * @author Ampflower
 * @since 0.1.0
 **/
@Mixin(Minecraft.class)
public class MixinMinecraft {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void musicmoods$returnHook(CallbackInfo ci) {
		WidgetAttachment.init((Minecraft) (Object) this);
	}
}
