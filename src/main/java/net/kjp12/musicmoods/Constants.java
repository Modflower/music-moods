/* Copyright 2023 KJP12
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package net.kjp12.musicmoods;// Created 2023-16-01T21:35:22

import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;

/**
 * @author KJP12
 * @since ${version}
 **/
public final class Constants {
	public static final ResourceLocation moodsResource = new ResourceLocation("music-moods",
			"textures/gui/widgets.png");

	public static final int atlasSize = 256;

	public static final int buttonHeight = Button.DEFAULT_HEIGHT;
	public static final int buttonWidth = Button.DEFAULT_WIDTH;
	public static final int twoColumnButtonOffset = 5;

	public static final int smallButtonWidth = 20;
	public static final int smallButtonOffset = 24;

	public static final int primaryButtonLeftOffset = buttonWidth + twoColumnButtonOffset;
	public static final int primaryButtonRightOffset = primaryButtonLeftOffset + twoColumnButtonOffset;
}
