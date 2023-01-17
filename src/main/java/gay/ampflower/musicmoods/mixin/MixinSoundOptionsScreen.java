/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;// Created 2023-16-01T13:54:29

import gay.ampflower.musicmoods.Config;
import gay.ampflower.musicmoods.Constants;
import gay.ampflower.musicmoods.client.ConfigurationScreen;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * @author Ampflower
 * @since 0.0.0
 **/
@Mixin(SoundOptionsScreen.class)
public class MixinSoundOptionsScreen extends OptionsSubScreen {

	public MixinSoundOptionsScreen(final Screen screen, final Options options, final Component component) {
		super(screen, options, component);
	}

	@Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/SoundOptionsScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/sounds/SoundSource;values()[Lnet/minecraft/sounds/SoundSource;")), locals = LocalCapture.CAPTURE_FAILHARD)
	private void musicmoods$addConfigButton(final CallbackInfo ci, final int yOffset, final int ySpacing,
			final int increment, final SoundSource[] $1, final int $2, final int $3, final SoundSource source) {
		// If the config option is disabled, no-op
		if (!Config.injectUiComponents) {
			return;
		}

		// If it's not MUSIC, continue.
		if (source != SoundSource.MUSIC) {
			return;
		}

		int x = this.width / 2 - Constants.primaryButtonLeftOffset;
		final int y = yOffset + ySpacing * (increment >> 1);

		if ((increment & 1) == 0) {
			x -= Constants.smallButtonOffset;
		} else {
			x += (Constants.primaryButtonRightOffset + Constants.smallButtonOffset);
		}

		this.addRenderableWidget(new ImageButton(x, y, Constants.smallButtonWidth, Constants.smallButtonWidth, 0, 0,
				Constants.smallButtonWidth, Constants.moodsResource, Constants.atlasSize, Constants.atlasSize,
				button -> this.minecraft.setScreen(new ConfigurationScreen(this))));
	}
}
