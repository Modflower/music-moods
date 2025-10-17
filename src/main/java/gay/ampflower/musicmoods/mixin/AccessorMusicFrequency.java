/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;

#if MC_1_21_6_OR_NEWER

import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * @author Ampflower
 * @since 0.6.10
 **/
@Mixin(MusicManager.MusicFrequency.class)
public interface AccessorMusicFrequency {
	@Accessor
	int getMaxFrequency();

	@Invoker
	int invokeGetNextSongDelay(@Nullable Music music, RandomSource random);
}

#endif
