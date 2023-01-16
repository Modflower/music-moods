/* Copyright 2023 KJP12
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package net.kjp12.musicmoods.client;// Created 2022-24-12T20:58:10

import com.mojang.logging.LogUtils;
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.SpruceTexts;
import dev.lambdaurora.spruceui.option.SpruceCheckboxBooleanOption;
import dev.lambdaurora.spruceui.option.SpruceIntegerInputOption;
import dev.lambdaurora.spruceui.option.SpruceSeparatorOption;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceOptionListWidget;
import dev.lambdaurora.spruceui.widget.container.tabbed.SpruceTabbedWidget;
import net.kjp12.musicmoods.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * @author KJP12
 * @since ${version}
 **/
public class ConfigurationScreen extends SpruceScreen {
	private static final Logger logger = LogUtils.getLogger();
	private static final MethodHandles.Lookup SELF = MethodHandles.lookup();

	private final Screen parent;

	private SpruceTabbedWidget tabbedWidget;

	protected ConfigurationScreen(final Screen parent) {
		super(Component.translatable("music-moods.gui.configuration"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();

		this.tabbedWidget = new SpruceTabbedWidget(Position.of(this, 0, 4), this.width, this.height - 35 - 4,
				this.title);

		this.tabbedWidget.addTabEntry(
				Component.translatable("music-moods.gui.configuration.music"), Component
						.translatable("music-moods.gui.configuration.music.description").withStyle(ChatFormatting.GRAY),
				ConfigurationScreen::buildMusicOptionList);

		this.addRenderableWidget(this.tabbedWidget);

		this.addRenderableWidget(new SpruceButtonWidget(Position.of(this, this.width / 2 - (150 / 2), this.height - 28),
				150, 20, SpruceTexts.GUI_DONE, btn -> onClose()));
	}

	@Override
	public void onClose() {
		try {
			Config.commit();
			minecraft.setScreen(parent);
		} catch (IOException ioe) {
			logger.error("Failed to save Music-Moods config", ioe);
			minecraft.setScreen(new ErrorScreen(Component.translatable("music-moods.gui.configuration.error"),
					Component.literal(ioe.getLocalizedMessage())));
		}
	}

	protected static SpruceOptionListWidget buildMusicOptionList(int width, int height) {
		final var list = new SpruceOptionListWidget(Position.origin(), width, height);

		try {
			list.addSingleOptionEntry(separator("situationalMusic"));
			list.addSingleOptionEntry(checkbox("allowReplacingCurrentMusic"));
			list.addOptionEntry(checkbox("immediatelyPlayOnReplace"), checkbox("alwaysPlayMusic"));
			list.addOptionEntry(intInput("fadeInTicks"), intInput("fadeOutTicks"));
		} catch (ReflectiveOperationException roe) {
			throw new AssertionError("Unexpected access violation", roe);
		}

		return list;
	}

	public static SpruceSeparatorOption separator(String field) {
		final var key = "music-moods.option.separator." + field;
		return new SpruceSeparatorOption(key, true, Component.translatable(key + ".description"));
	}

	public static SpruceIntegerInputOption intInput(String field) throws IllegalAccessException, NoSuchFieldException {
		final var handle = SELF.findStaticVarHandle(Config.class, field, int.class);
		final var key = "music-moods.option." + field;
		return new SpruceIntegerInputOption(key, () -> (int) handle.get(), handle::set,
				Component.translatable(key + ".description"));
	}

	public static SpruceCheckboxBooleanOption checkbox(String field)
			throws IllegalAccessException, NoSuchFieldException {
		final var handle = SELF.findStaticVarHandle(Config.class, field, boolean.class);
		final var key = "music-moods.option." + field;
		return new SpruceCheckboxBooleanOption(key, () -> (boolean) handle.get(), handle::set,
				Component.translatable(key + ".description"));
	}
}