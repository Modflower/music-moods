/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package net.kjp12.musicmoods.mixin;// Created 2023-11-01T06:47:10

import net.kjp12.musicmoods.client.WeighedSoundEventsQuery;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final Logger logger = LoggerFactory.getLogger("Music Moods Weighed Sounds Query");

	@Shadow
	@Final
	private List<Weighted<Sound>> list;

	@Unique
	private Set<ResourceLocation> set = Set.of();

	@Override
	public boolean contains(final Sound sound) {
		final var set = this.getSet();

		return set.contains(sound.getLocation());
	}

	@Unique
	private Set<ResourceLocation> getSet() {
		final var size = this.list.size();
		final var soundSet = this.set;
		if (size != soundSet.size()) {
			final var newSoundSet = new HashSet<ResourceLocation>(size);

			for (final Weighted<Sound> weightedSound : this.list) {
				if (weightedSound instanceof Sound subSound) {
					newSoundSet.add(subSound.getLocation());
				} else if (weightedSound instanceof MixinWeighedSoundEvents subWeighed && this != subWeighed) {
					newSoundSet.addAll(subWeighed.getSet());
				}
			}

			return this.set = Set.of(newSoundSet.toArray(new ResourceLocation[newSoundSet.size()]));
		}

		return soundSet;
	}
}
