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
import dev.lambdaurora.spruceui.option.SpruceCyclingOption;
import dev.lambdaurora.spruceui.option.SpruceIntegerInputOption;
import dev.lambdaurora.spruceui.option.SpruceSeparatorOption;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceOptionListWidget;
import dev.lambdaurora.spruceui.widget.container.tabbed.SpruceTabbedWidget;
import net.kjp12.musicmoods.ClientMain;
import net.kjp12.musicmoods.Config;
import net.kjp12.musicmoods.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author KJP12
 * @since 0.0.0
 **/
public class ConfigurationScreen extends SpruceScreen {
	private static final Logger logger = LogUtils.getLogger();
	private static final MethodHandles.Lookup SELF = MethodHandles.lookup();

	private final Screen parent;

	private SpruceTabbedWidget tabbedWidget;

	private boolean committed;

	public ConfigurationScreen(final Screen parent) {
		super(Component.translatable("music-moods.gui.configuration"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();

		this.tabbedWidget = new SpruceTabbedWidget(Position.of(this, 0, 4), this.width, this.height - 35 - 4,
				this.title);

		addTabEntry("music", ConfigurationScreen::buildMusicOptionList);
		addTabEntry("meta", ConfigurationScreen::buildMetaList);

		this.addRenderableWidget(this.tabbedWidget);

		this.addRenderableWidget(new SpruceButtonWidget(
				Position.of(this, this.width / 2 - (Constants.buttonWidth / 2), this.height - 28),
				Constants.buttonWidth, Constants.buttonHeight, SpruceTexts.GUI_DONE, btn -> onClose()));
	}

	@Override
	public void removed() {
		if (!committed) {
			try {
				Config.commit();
			} catch (IOException ioe) {
				logger.error("Failed to save Music-Moods config. User not notified.", ioe);
			}
		}
	}

	@Override
	public void onClose() {
		try {
			committed = true;
			Config.commit();
			minecraft.setScreen(parent);
		} catch (IOException ioe) {
			logger.error("Failed to save Music-Moods config", ioe);
			minecraft.setScreen(new ErrorScreen(Component.translatable("music-moods.gui.configuration.error"),
					Component.literal(ioe.getLocalizedMessage())));
		}
	}

	protected void addTabEntry(String name, SpruceTabbedWidget.ContainerFactory factory) {
		final var key = "music-moods.gui.configuration." + name;
		this.tabbedWidget.addTabEntry(Component.translatable(key),
				Component.translatable(key + ".description").withStyle(ChatFormatting.GRAY), factory);
	}

	protected static SpruceOptionListWidget buildMusicOptionList(int width, int height) {
		final var list = new SpruceOptionListWidget(Position.origin(), width, height);

		try {
			list.addSingleOptionEntry(separator("situationalMusic"));
			list.addSingleOptionEntry(cycling("situationalMusicReplacing"));
			list.addOptionEntry(checkbox("immediatelyPlayOnReplace"), checkbox("alwaysPlayMusic"));
			list.addOptionEntry(intInput("fadeInTicks"), intInput("fadeOutTicks"));
		} catch (ReflectiveOperationException roe) {
			throw new AssertionError("Unexpected access violation", roe);
		}

		return list;
	}

	protected static SpruceOptionListWidget buildMetaList(int width, int height) {
		final var list = new SpruceOptionListWidget(Position.origin(), width, height);

		try {
			list.addSingleOptionEntry(separator("modPack"));
			if (ClientMain.isModMenuPresent) {
				list.addSingleOptionEntry(checkbox("injectUiComponents"));
			} else {
				// TODO: Make a proper widget for this.
				list.addSingleOptionEntry(separator("modMenu"));
			}
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

	public static SpruceCyclingOption cycling(String field) throws IllegalAccessException, NoSuchFieldException {
		final var handle = findEnumField(field);
		final var enums = (Enum<?>[]) handle.varType().getEnumConstants();
		final var key = "music-moods.option." + field;
		final var stepper = new EnumStepper(enums, handle, key);
		return new SpruceCyclingOption(key, stepper, stepper, Component.translatable(key + ".description"));
	}

	private static VarHandle findEnumField(String name) throws IllegalAccessException, NoSuchFieldException {
		final var fields = Config.class.getFields();
		for (final var field : fields) {
			if (name.equals(field.getName())) {
				if (Enum.class.isAssignableFrom(field.getType())) {
					return SELF.unreflectVarHandle(field);
				}
				throw new NoSuchFieldException("Incompatible field " + field);
			}
		}
		throw new NoSuchFieldException("Cannot find " + name + " in Config");
	}

	private record EnumStepper(Enum<?>[] enums, VarHandle handle, String key)
			implements Consumer<Integer>, Function<SpruceCyclingOption, Component> {

		@Override
		public void accept(final Integer integer) {
			final int newIndex = (get().ordinal() + integer) % this.enums.length;
			this.handle.set(this.enums[newIndex]);
		}

		private Enum<?> get() {
			return (Enum<?>) this.handle.get();
		}

		@Override
		public Component apply(final SpruceCyclingOption self) {
			return Component.translatable(key,
					Component.translatable("music-moods.option.value." + get().name().toLowerCase(Locale.ROOT)));
		}
	}
}
