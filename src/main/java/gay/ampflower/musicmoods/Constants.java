/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods;// Created 2023-16-01T21:35:22

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Ampflower
 * @since 0.0.0
 **/
public final class Constants {
	public static final String modId = "music-moods";

	public static final int buttonHeight = Button.DEFAULT_HEIGHT;
	public static final int buttonWidth = Button.DEFAULT_WIDTH;
	public static final int twoColumnButtonOffset = 5;

	public static final int smallButtonWidth = 20;
	public static final int smallButtonPlacementOffset = 4;
	public static final int smallButtonOffset = smallButtonWidth + smallButtonPlacementOffset;

	public static final int primaryButtonLeftOffset = buttonWidth + twoColumnButtonOffset;
	public static final int primaryButtonRightOffset = primaryButtonLeftOffset + twoColumnButtonOffset;

	private static final Component rightMouseButton = Component.translatable("key.mouse.right")
		.withStyle(ChatFormatting.GRAY);

	public static final Component rightClickToPlayTooltip = Component.translatable(
		"music-moods.inventory.rightClickToPlay.play",
			rightMouseButton
		)
		.withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);

	public static final Component rightClickToStopTooltip = Component.translatable(
			"music-moods.inventory.rightClickToPlay.stop",
			rightMouseButton
		)
		.withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);

	public static ResourceLocation toStereo(ResourceLocation mono) {
		return mono.withPrefix("stereo/");
	}

	public static ResourceLocation id(String path) {
		#if MC_1_20_5_OR_OLDER
		return new ResourceLocation(modId, path);
		#else
		return ResourceLocation.fromNamespaceAndPath(modId, path);
		#endif
	}
}
