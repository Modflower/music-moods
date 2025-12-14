/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;// Created 2023-11-01T06:47:10

import gay.ampflower.musicmoods.client.WeighedSoundEventsQuery;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.Weighted;
#if MC_1_21_11_OR_NEWER
import net.minecraft.resources.Identifier;
#else
import net.minecraft.resources.ResourceLocation;
#endif
#if MC_1_16_4_OR_OLDER
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
#else
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
#endif
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ampflower
 * @since 0.0.0
 **/
@Mixin(WeighedSoundEvents.class)
public class MixinWeighedSoundEvents implements WeighedSoundEventsQuery {
	@Unique
	#if MC_1_16_4_OR_OLDER
	private static final Logger logger = LogManager.getLogger("Music Moods Weighed Sounds Query");
	#else
	private static final Logger logger = LoggerFactory.getLogger("Music Moods Weighed Sounds Query");
	#endif

	@Shadow
	@Final
	private List<Weighted<Sound>> list;

	@Unique
	private Set<#if (MC_1_21_11_OR_NEWER) Identifier #else ResourceLocation #endif > set = Set.of();

	@Override
	public boolean contains(final Sound sound) {
		final var set = this.getSet();

		return set.contains(sound.getLocation());
	}

	@Unique
	private Set<#if (MC_1_21_11_OR_NEWER) Identifier #else ResourceLocation #endif > getSet() {
		final var size = this.list.size();
		final var soundSet = this.set;
		if (size != soundSet.size()) {
			final var newSoundSet =
				new HashSet<#if (MC_1_21_11_OR_NEWER) Identifier #else ResourceLocation #endif >(size);

			for (final Weighted<Sound> weightedSound : this.list) {
				if (weightedSound instanceof Sound subSound) {
					newSoundSet.add(subSound.getLocation());
				} else if (weightedSound instanceof MixinWeighedSoundEvents subWeighed && this != subWeighed) {
					newSoundSet.addAll(subWeighed.getSet());
				}
			}

			return this.set = Set.of(newSoundSet.toArray(
				new #if (MC_1_21_11_OR_NEWER) Identifier #else ResourceLocation #endif [newSoundSet.size()]
			));
		}

		return soundSet;
	}
}
