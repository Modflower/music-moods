/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;

#if MC_1_21_2_OR_NEWER
import net.minecraft.client.renderer.LevelEventHandler;
#else
import net.minecraft.client.renderer.LevelRenderer;
#endif
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * @author Ampflower
 * @since 0.6.10
 **/
@Mixin(#if (MC_1_21_2_OR_NEWER) LevelEventHandler.class #else LevelRenderer.class #endif )
public interface AccessorLevelEventHandler {
	@Accessor#if (MC_1_20_5_OR_OLDER) ("playingRecords") #endif
	Map<BlockPos, SoundInstance> getPlayingJukeboxSongs();
}
