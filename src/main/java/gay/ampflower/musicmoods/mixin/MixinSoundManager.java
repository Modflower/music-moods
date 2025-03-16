/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;

import gay.ampflower.musicmoods.client.SoundHandler;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author Ampflower
 * @since 0.6.9
 **/
@Mixin(SoundManager.class)
public class MixinSoundManager implements SoundHandler {
	@Shadow
	@Final
	private SoundEngine soundEngine;

	@Override
	public void moods$stopMusic() {
		((SoundHandler) soundEngine).moods$stopMusic();
	}

	@Override
	public void moods$stopSounds() {
		((SoundHandler) soundEngine).moods$stopSounds();
	}

	@Override
	public void moods$fadeSounds(final float ticks) {
		((SoundHandler) soundEngine).moods$fadeSounds(ticks);
	}

	@Override
	public void moods$clearQueued() {
		((SoundHandler) soundEngine).moods$clearQueued();
	}
}
