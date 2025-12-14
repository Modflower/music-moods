/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods;// Created 2023-16-01T21:35:22

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
#if MC_1_16_4_OR_OLDER
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
#else
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
#endif

#if MC_1_21_11_OR_NEWER
import net.minecraft.resources.Identifier;
#else
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
#endif

#if MC_1_18_OR_OLDER
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
#else
import static net.minecraft.network.chat.Component.translatable;
#endif

/**
 * @author Ampflower
 * @since 0.0.0
 **/
public final class Constants {
	private static final StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	public static final String modId = "music-moods";

	#if MC_1_18_OR_OLDER
	public static final int buttonHeight = 20;
	public static final int buttonWidth = 150;
	#else
	public static final int buttonHeight = Button.DEFAULT_HEIGHT;
	public static final int buttonWidth = Button.DEFAULT_WIDTH;
	#endif
	public static final int twoColumnButtonOffset = 5;

	public static final int smallButtonWidth = 20;
	public static final int smallButtonPlacementOffset = 4;
	public static final int smallButtonOffset = smallButtonWidth + smallButtonPlacementOffset;

	public static final int primaryButtonLeftOffset = buttonWidth + twoColumnButtonOffset;
	public static final int primaryButtonRightOffset = primaryButtonLeftOffset + twoColumnButtonOffset;

	private static final Component rightMouseButton = translatable("key.mouse.right")
		.withStyle(ChatFormatting.GRAY);

	public static final Component rightClickToPlayTooltip = translatable(
		"music-moods.inventory.rightClickToPlay.play",
		rightMouseButton
	)
		.withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);

	public static final Component rightClickToStopTooltip = translatable(
		"music-moods.inventory.rightClickToPlay.stop",
		rightMouseButton
	)
		.withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);

	public static #if (MC_1_21_11_OR_NEWER) Identifier #else ResourceLocation #endif toStereo(
		final #if (MC_1_21_11_OR_NEWER) Identifier #else ResourceLocation #endif mono
	) {
		return mono.withPrefix("stereo/");
	}

	public static #if (MC_1_21_11_OR_NEWER) Identifier #else ResourceLocation #endif id(String path) {
		#if MC_1_20_5_OR_OLDER
		return new ResourceLocation(modId, path);
		#elif MC_1_21_11_OR_NEWER
		return Identifier.fromNamespaceAndPath(modId, path);
		#else
		return ResourceLocation.fromNamespaceAndPath(modId, path);
		#endif
	}

	#if MC_1_18_OR_OLDER
	@ApiStatus.Internal
	public static TranslatableComponent translatable(final String key) {
		return new TranslatableComponent(key);
	}

	@ApiStatus.Internal
	public static TranslatableComponent translatable(final String key, final Object... values) {
		return new TranslatableComponent(key, values);
	}

	@ApiStatus.Internal
	public static TextComponent literal(final String string) {
		return new TextComponent(string);
	}

	#endif

	#if MC_1_16_4_OR_OLDER
	public static Logger getLogger() {
		return LogManager.getLogger(walker.callerClass);
	}
	#else
	public static Logger getLogger() {
		return LoggerFactory.getLogger(walker.callerClass);
	}
	#endif
}
