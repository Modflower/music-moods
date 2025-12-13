/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.client;// Created 2022-24-12T20:58:10

import com.mojang.logging.LogUtils;
#if !FORGE
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.SpruceTexts;
import dev.lambdaurora.spruceui.option.SpruceCheckboxBooleanOption;
import dev.lambdaurora.spruceui.option.SpruceCyclingOption;
import dev.lambdaurora.spruceui.option.SpruceDoubleOption;
import dev.lambdaurora.spruceui.option.SpruceFloatInputOption;
import dev.lambdaurora.spruceui.option.SpruceIntegerInputOption;
import dev.lambdaurora.spruceui.option.SpruceSeparatorOption;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.SpruceWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceOptionListWidget;
import dev.lambdaurora.spruceui.widget.container.tabbed.SpruceTabbedWidget;
#else
import org.thinkingstudio.obsidianui.Position;
import org.thinkingstudio.obsidianui.SpruceTexts;
import org.thinkingstudio.obsidianui.option.SpruceCheckboxBooleanOption;
import org.thinkingstudio.obsidianui.option.SpruceCyclingOption;
import org.thinkingstudio.obsidianui.option.SpruceDoubleOption;
import org.thinkingstudio.obsidianui.option.SpruceFloatInputOption;
import org.thinkingstudio.obsidianui.option.SpruceIntegerInputOption;
import org.thinkingstudio.obsidianui.option.SpruceSeparatorOption;
import org.thinkingstudio.obsidianui.screen.SpruceScreen;
import org.thinkingstudio.obsidianui.widget.SpruceButtonWidget;
import org.thinkingstudio.obsidianui.widget.SpruceWidget;
import org.thinkingstudio.obsidianui.widget.container.SpruceOptionListWidget;
import org.thinkingstudio.obsidianui.widget.container.tabbed.SpruceTabbedWidget;
#endif
import gay.ampflower.musicmoods.ClientMain;
import gay.ampflower.musicmoods.Config;
import gay.ampflower.musicmoods.Constants;
import gay.ampflower.musicmoods.Mint;
import gay.ampflower.musicmoods.config.OptionEnum;
import net.minecraft.ChatFormatting;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

#if MC_1_21_6_OR_NEWER
import dev.lambdaurora.spruceui.tooltip.TooltipData;
import gay.ampflower.musicmoods.mixin.AccessorOptionInstance;
import gay.ampflower.musicmoods.mixin.AccessorTooltip;
import net.minecraft.client.gui.components.Tooltip;
#endif

/**
 * @author Ampflower
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

		this.tabbedWidget = new SpruceTabbedWidget(
			Position.of(this, 0, 4),
			this.width,
			this.height - 35 - 4,
			this.title
		);

		addTabEntry("volume", this::buildVolumeList);
		addTabEntry("music", this::buildMusicOptionList);
		addTabEntry("jukebox", ConfigurationScreen::buildJukeboxOptionList);
		addTabEntry("demo", ConfigurationScreen::buildDemoList);
		addTabEntry("meta", ConfigurationScreen::buildMetaList);

		this.addRenderableWidget(this.tabbedWidget);

		this.addRenderableWidget(new SpruceButtonWidget(
			Position.of(this, this.width / 2 - (Constants.buttonWidth / 2), this.height - 28),
			Constants.buttonWidth, Constants.buttonHeight, SpruceTexts.GUI_DONE, btn -> onClose()
		));
	}

	@Override
	public void removed() {
		if (!committed) {
			try {
				Config.commit();
				minecraft.options.save();
			} catch (IOException ioe) {
				logger.error("Failed to save Music Moods config. User not notified.", ioe);
			}
		}
	}

	@Override
	public void onClose() {
		try {
			committed = true;
			Config.commit();
			minecraft.options.save();
			minecraft.setScreen(parent);
		} catch (IOException ioe) {
			logger.error("Failed to save Music Moods config", ioe);
			minecraft.setScreen(new ErrorScreen(
				Component.translatable("music-moods.gui.configuration.error"),
				Component.literal(ioe.getLocalizedMessage())
			));
		}
	}

	protected void addTabEntry(String name, SpruceTabbedWidget.ContainerFactory factory) {
		final var key = "music-moods.gui.configuration." + name;
		this.tabbedWidget.addTabEntry(
			Component.translatable(key),
			Component.translatable(key + ".description").withStyle(ChatFormatting.GRAY),
			factory
		);
	}

	protected SpruceOptionListWidget buildVolumeList(int width, int height) {
		final var list = new SpruceOptionListWidget(Position.origin(), width, height);

		SoundSource last = null;

		for (final var value : SoundSource.values()) {
			if (value == SoundSource.MASTER) {
				list.addSingleOptionEntry(toSlider(value));
				continue;
			}

			if (last == null) {
				last = value;
				continue;
			}

			list.addOptionEntry(toSlider(last), toSlider(value));
			last = null;
		}

		if (last != null) {
			list.addSmallSingleOptionEntry(toSlider(last));
		}

		list.addSingleOptionEntry(soundDevices);

		list.addOptionEntry(
			toCheckbox(
				"showSubtitles",
				minecraft.options.showSubtitles(),
				Component.translatable("options.showSubtitles.tooltip")
			),
			toCheckbox(
				"directionalAudio",
				minecraft.options.directionalAudio(),
				Component.translatable("options.directionalAudio.tooltip")
			)
		);

		#if MC_1_21_6_OR_NEWER
		// Music tab primarily should house these, but it is here in vanilla's.
		list.addOptionEntry(
			musicFrequency,
			toCheckbox(
				"showNowPlayingToast",
				minecraft.options.showNowPlayingToast(),
				Component.translatable("options.showNowPlayingToast.tooltip")
			)
		);
		#else;
		try {
			list.addSingleOptionEntry(checkbox("alwaysPlayMusic"));
		} catch (ReflectiveOperationException roe) {
			throw new AssertionError("Unexpected access violation", roe);
		}
		#endif

		return list;
	}

	protected SpruceOptionListWidget buildMusicOptionList(int width, int height) {
		final var list = new SpruceOptionListWidget(Position.origin(), width, height);

		try {
			list.addSingleOptionEntry(separator("situationalMusic"));
			list.addSingleOptionEntry(cycling("situationalMusicReplacing"));
			#if MC_1_21_6_OR_NEWER
			list.addOptionEntry(checkbox("immediatelyPlayOnReplace"), musicFrequency);
			#else
			list.addOptionEntry(checkbox("immediatelyPlayOnReplace"), checkbox("alwaysPlayMusic"));
			#endif
			list.addSingleOptionEntry(separator("transitions"));
			list.addOptionEntry(intInput("fadeInTicks"), intInput("fadeOutTicks"));
			#if MC_1_21_6_OR_NEWER
			list.addSingleOptionEntry(checkbox("seamlessTransitions"));
			#else
			list.addOptionEntry(checkbox("seamlessTransitions"), checkbox("allowPausingMusic"));
			#endif
		} catch (ReflectiveOperationException roe) {
			throw new AssertionError("Unexpected access violation", roe);
		}

		return list;
	}

	protected static SpruceOptionListWidget buildJukeboxOptionList(int width, int height) {
		final var list = new SpruceOptionListWidget(Position.origin(), width, height);

		try {
			list.addSingleOptionEntry(separator("jukebox"));
			list.addOptionEntry(checkbox("jukeboxEnabled"), checkbox("jukeboxMultiplayer"));
			list.addSingleOptionEntry(separator("jukeboxRange"));
			list.addOptionEntry(floatSlider("jukeboxReplaceRange"), floatSlider("jukeboxFadeRange"));
			list.addSingleOptionEntry(separator("transitions"));
			list.addOptionEntry(intInput("jukeboxFadeMixTicks"), intInput("jukeboxFadeStopTicks"));
		} catch (ReflectiveOperationException roe) {
			throw new AssertionError("Unexpected access violation", roe);
		}

		return list;
	}

	protected static SpruceOptionListWidget buildDemoList(int width, int height) {
		final var list = new SpruceOptionListWidget(Position.origin(), width, height);

		try {
			list.addSingleOptionEntry(separator("modfest"));
			list.addSingleOptionEntry(checkbox("rightClickToPlay"));
		} catch (ReflectiveOperationException roe) {
			throw new AssertionError(roe);
		}

		return list;
	}

	protected static SpruceOptionListWidget buildMetaList(int width, int height) {
		final var list = new SpruceOptionListWidget(Position.origin(), width, height);

		try {
			list.addSingleOptionEntry(separator("modPack"));
			list.addSingleOptionEntry(checkbox("injectUiComponents", () -> ClientMain.isModMenuPresent));
		} catch (ReflectiveOperationException roe) {
			throw new AssertionError("Unexpected access violation", roe);
		}

		return list;
	}

	#if MC_1_19_OR_OLDER
	private SpruceDoubleOption toSlider(final SoundSource source) {
		final var key = "soundCategory." + source.name;
		return new SpruceDoubleOption(
			key,
			0,
			1,
			0.01F,
			() -> (double)minecraft.options.getSoundSourceVolume(source),
			value -> minecraft.options.setSoundCategoryVolume(source, value.floatValue()),
			self -> genericPercentage(key, self.get()),
			translation(key + ".description")
		);
	}
	#else
	private SpruceDoubleOption toSlider(final SoundSource source) {
		final var key = "soundCategory." + source.name;
		final var option = minecraft.options.getSoundSourceOptionInstance(source);
		return new SpruceDoubleOption(
			key,
			0,
			1,
			0.01F,
			option::get,
			option::set,
			self -> genericPercentage(key, self.get()),
			translation(key + ".description")
		);
	}
	#endif

	private static SpruceCheckboxBooleanOption toCheckbox(
		final String name,
		final OptionInstance<Boolean> option,
		final Component tooltip
	) {
		final var key = "options." + name;

		return new SpruceCheckboxBooleanOption(key, option::get, option::set, adapt(tooltip));
	}

	private SpruceCyclingOption getSoundDevices() {
		final var key = "options.soundDevice";
		final var option = minecraft.options.soundDevice();

		final var stepper = new DynamicStepper<String>(
			() -> {
				final var init = minecraft.soundManager.availableSoundDevices;
				final var list = new ArrayList<String>(init.size() + 1);
				list.add("");
				list.addAll(init);
				return list;
			},
			option::get,
			option::set,
			str -> {
				if ("".equals(str)) {
					return genericValue("options.audioDevice", "options.audioDevice.default");
				}

				if (str.startsWith(SoundEngine.OPEN_AL_SOFT_PREFIX)) {
					str = str.substring(SoundEngine.OPEN_AL_SOFT_PREFIX_LENGTH);
				}

				return genericValue(Component.translatable("options.audioDevice"), Component.literal(str));
			}
		);

		final var nl = Component.literal("\n- ");
		final var tooltip = Component.translatable(key + ".tooltip");

		for (var device : minecraft.soundManager.availableSoundDevices) {
			if (device.startsWith(SoundEngine.OPEN_AL_SOFT_PREFIX)) {
				device = device.substring(SoundEngine.OPEN_AL_SOFT_PREFIX_LENGTH);
			}

			tooltip.append(nl).append(Component.literal(device).withStyle(ChatFormatting.YELLOW));
		}

		return new SpruceCyclingOption(key, stepper, stepper, adapt(tooltip));
	}

	#if MC_1_21_6_OR_NEWER
	private SpruceCyclingOption getMusicFrequency() {
		final var key = "options.music_frequency";
		final var option = minecraft.options.musicFrequency();

		final var stepper = new EnumStepper<>(
			MusicManager.MusicFrequency.values(),
			option::get,
			option::set,
			value -> genericValue(key, value.getKey())
		);

		return new SpruceCyclingOption(key, stepper, stepper, translation(key + ".tooltip"));
	}
	#endif

	public static SpruceSeparatorOption separator(String field) {
		final var key = "music-moods.option.separator." + field;
		return new SpruceSeparatorOption(key, true, translation(key + ".description"));
	}

	public static SpruceFloatInputOption floatSlider(String field) throws IllegalAccessException, NoSuchFieldException {
		final var handle = SELF.findStaticVarHandle(Config.class, field, float.class);
		final var key = "music-moods.option." + field;
		return new SpruceFloatInputOption(
			key,
			() -> (float) handle.get(),
			handle::set,
			translation(key + ".description")
		);
	}

	public static SpruceIntegerInputOption intInput(String field) throws IllegalAccessException, NoSuchFieldException {
		final var handle = SELF.findStaticVarHandle(Config.class, field, int.class);
		final var key = "music-moods.option." + field;
		return new SpruceIntegerInputOption(
			key,
			() -> (int) handle.get(),
			handle::set,
			translation(key + ".description")
		);
	}

	public static SpruceCheckboxBooleanOption checkbox(String field)
		throws IllegalAccessException, NoSuchFieldException {
		final var handle = SELF.findStaticVarHandle(Config.class, field, boolean.class);
		final var key = "music-moods.option." + field;
		return new SpruceCheckboxBooleanOption(
			key,
			() -> (boolean) handle.get(),
			handle::set,
			translation(key + ".description")
		);
	}

	public static SpruceCheckboxBooleanOption checkbox(String field, BooleanSupplier isActive)
		throws IllegalAccessException, NoSuchFieldException {
		final var handle = SELF.findStaticVarHandle(Config.class, field, boolean.class);
		final var key = "music-moods.option." + field;
		return new SpruceCheckboxBooleanOption(
			key,
			() -> (boolean) handle.get(),
			handle::set,
			translation(key + ".description")
		) {
			@Override
			public SpruceWidget createWidget(final Position position, final int width) {
				final var widget = super.createWidget(position, width);
				widget.isActive = isActive.asBoolean;
				return widget;
			}
		};
	}

	public static SpruceCyclingOption cycling(String field) throws IllegalAccessException, NoSuchFieldException {
		final var handle = findEnumField(field);
		final var enums = (Enum<?>[]) handle.varType().getEnumConstants();
		final var key = "music-moods.option." + field;
		final var stepper = new EnumStepper(enums, handle, key);
		return new SpruceCyclingOption(key, stepper, stepper, translation(key + ".description"));
	}

	private static <T extends Enum<T>> SpruceCyclingOption cycling(
		final String key,
		final Class<T> supplier,
		final Supplier<T> getter,
		final Consumer<T> setter,
		final Function<T, Component> toName
	) {
		final var stepper = new EnumStepper<T>(supplier.getEnumConstants(), getter, setter, toName);

		return new SpruceCyclingOption(
			key,
			stepper,
			stepper,
			translation(key + ".description")
		);
	}

	private static <T> SpruceCyclingOption cycling(
		final String key,
		final Supplier<List<T>> supplier,
		final Supplier<T> getter,
		final Consumer<T> setter,
		final Function<T, Component> toName
	) {
		final var stepper = new DynamicStepper<T>(supplier, getter, setter, toName);
		return new SpruceCyclingOption(
			key,
			stepper,
			stepper,
			translation(key + ".description")
		);
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

	private static Component genericValue(final String key, final String value) {
		return genericValue(Component.translatable(key), Component.translatable(value));
	}

	private static Component genericValue(final Component key, final Component value) {
		return Component.translatable("options.generic_value", key, value);
	}

	private static Component genericPercentage(final String key, final double value) {
		return genericPercentage(Component.translatable(key), (int) (value * 100.D));
	}

	private static Component genericPercentage(final Component key, final int value) {
		return Component.translatable("options.percent_value", key, value);
	}

	#if MC_1_21_5_OR_OLDER
	private static Component translation(final String key) {
		return Component.translatable(key);
	}
	private static Component adapt(final Component component) {
		return component;
	}
	#else
	private static TooltipData translation(final String key) {
		return adapt(Component.translatable(key));
	}

	private static TooltipData adapt(final Component component) {
		if (component == null || Component.empty().equals(component)) {
			return TooltipData.EMPTY;
		}
		return TooltipData.builder().text(component).build();
	}

	private static TooltipData adapt(final Tooltip tooltip) {
		if (tooltip == null) {
			return TooltipData.EMPTY;
		}
		return adapt(((AccessorTooltip) tooltip).message);
	}

	private static <T> TooltipData adapt(final OptionInstance<T> instance) {
		return adapt(((AccessorOptionInstance<T>) (Object) instance).tooltip.apply(instance.get()));
	}
	#endif

	private record EnumStepper<T extends Enum<T>>(
		T[] enums,
		Supplier<T> getter,
		Consumer<T> setter,
		Function<T, Component> toName
	) implements Consumer<Integer>, Function<SpruceCyclingOption, Component> {

		private EnumStepper(final T[] enums, final VarHandle handle, final String key) {
			this(enums, () -> (T) handle.get(), handle::set, value -> {
				final var name = value.name().toLowerCase(Locale.ROOT);

				if (value instanceof OptionEnum option) {
					return Component.translatable(
						key,
						Component.translatable("music-moods.option.value." + option.localizationClass() + "." + name)
					);
				}

				return Component.translatable(key, Component.translatable("music-moods.option.value." + name));
			});
		}

		@Override
		public void accept(final Integer integer) {
			final int newIndex = (this.getter.get().ordinal() + integer) % this.enums.length;
			this.setter.accept(this.enums[newIndex]);
		}

		@Override
		public Component apply(final SpruceCyclingOption self) {
			return this.toName.apply(this.getter.get());
		}
	}

	private record DynamicStepper<T>(
		Supplier<List<T>> supplier,
		Supplier<T> getter,
		Consumer<T> setter,
		Function<T, Component> toName
	) implements Consumer<Integer>, Function<SpruceCyclingOption, Component> {

		@Override
		public void accept(final Integer integer) {
			final var list = this.supplier.get();
			final var curr = this.getter.get();
			final var index = list.indexOf(curr);
			final int newIndex = Mint.wrapPositive(index + integer, list.size());
			this.setter.accept(list.get(newIndex));
		}

		@Override
		public Component apply(final SpruceCyclingOption spruceCyclingOption) {
			return this.toName.apply(getter.get());
		}
	}
}
