/*
 * Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package gay.ampflower.musicmoods.util;

import gay.ampflower.musicmoods.Config;
import gay.ampflower.musicmoods.Constants;
import gay.ampflower.musicmoods.client.MusicHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;

import net.minecraft.world.item.ItemStack;

#if MC_1_21
import net.minecraft.resources.ResourceKey;
#endif

#if MC_1_20_5_OR_OLDER
import gay.ampflower.musicmoods.Sounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.RecordItem;
#else
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.JukeboxSong;
#endif

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Ampflower
 * @since 0.7
 **/
public final class JukeboxUtil {

	public static void appendRightClickToPlay(
		final Consumer<Component> tooltipConsumer,
		#if MC_1_20_5_OR_OLDER
		final Function<RegistryAccess, Optional<SoundEvent>> song
		#else
		final Function<RegistryAccess, Optional<JukeboxSong>> song
		#endif
	) {
		if (!Config.rightClickToPlay) {
			return;
		}

		final var minecraft = Minecraft.getInstance();
		final var level = minecraft.level;

		if (level == null) {
			return;
		}

		final var optionalSong = song.apply(level.registryAccess());

		if (optionalSong.isEmpty()) {
			return;
		}

		final var musicHandler = (MusicHandler) minecraft.getMusicManager();

		tooltipConsumer.accept(
			musicHandler.moods$isCurrentlyPlaying(optionalSong.get())
				? Constants.rightClickToStopTooltip
				: Constants.rightClickToPlayTooltip
		);
	}

	#if MC_1_20_5_OR_OLDER

	public static Optional<SoundEvent> toStereoElseMono(
		final ItemStack stack
	) {
		if (stack == null || stack.isEmpty) {
			return Optional.empty();
		}

		if (!(stack.getItem() instanceof RecordItem record)) {
			return Optional.empty();
		}

		return Optional.of(Sounds.findStereo(record.sound));
	}

	#else

	public static Optional<JukeboxSong> toStereoElseMono(
		final RegistryAccess access,
		final EitherHolder<JukeboxSong> mono
	) {
		return toStereoElseMono(access, mono.unwrap(access));
	}

	public static Optional<JukeboxSong> toStereoElseMono(
		final RegistryAccess access,
		final ItemStack itemStack
	) {
		if (itemStack == null || itemStack.isEmpty()) {
			return Optional.empty();
		}

		return toStereoElseMono(access, JukeboxSong.fromStack(access, itemStack));
	}

	public static Optional<JukeboxSong> toStereoElseMono(
		final RegistryAccess access,
		final Optional<Holder<JukeboxSong>> optionalMono
	) {
		if (optionalMono.isEmpty()) {
			return Optional.empty();
		}

		final var mono = optionalMono.get();
		return mono
			.unwrapKey()
			.flatMap(k -> access
						 .lookup(Registries.JUKEBOX_SONG)
						 .orElseThrow()
						 #if MC_1_21_OR_OLDER
							 .get(ResourceKey.create(Registries.JUKEBOX_SONG, Constants.toStereo(k.location())))
							 .map(Holder.Reference::value)
						 #else
						 .getOptional(Constants.toStereo(k.location()))
					#endif
			)
			.or(() -> Optional.of(mono.value()));
	}
	#endif
}
