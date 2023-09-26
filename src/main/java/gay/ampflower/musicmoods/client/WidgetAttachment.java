/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.client;// Created 2023-17-01T20:34:47

import gay.ampflower.musicmoods.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ampflower
 * @since 0.1.0
 **/
public final class WidgetAttachment {
	public static final Map<OptionInstance<?>, WidgetFactory> binding = new HashMap<>();

	public static void add(OptionInstance<?> instance, WidgetFactory factory) {
		binding.put(instance, factory);
	}

	public static void init(final Minecraft minecraft) {
		WidgetAttachment.add(minecraft.options.getSoundSourceOptionInstance(SoundSource.MUSIC), (widget, first) -> {
			final int x = deriveX(widget, first, Constants.smallButtonWidth, Constants.smallButtonPlacementOffset);
			final int y = widget.getY();

			return new ImageButton(x, y, Constants.smallButtonWidth, Constants.smallButtonWidth, Constants.musicSprites,
					button -> minecraft.setScreen(new ConfigurationScreen(minecraft.screen)));
		});
	}

	public static int deriveX(final AbstractWidget reference, final boolean before, final int width, final int offset) {
		if (before) {
			return reference.getX() - width - offset;
		} else {
			return reference.getX() + reference.getWidth() + offset;
		}
	}

	@FunctionalInterface
	public interface WidgetFactory {
		AbstractWidget createWidget(AbstractWidget widget, boolean first);
	}
}
