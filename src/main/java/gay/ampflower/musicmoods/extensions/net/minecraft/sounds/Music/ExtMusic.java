/*
 * Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package gay.ampflower.musicmoods.extensions.net.minecraft.sounds.Music;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;

/**
 * @author Ampflower
 * @since 0.7
 **/
@Extension
public final class ExtMusic {
	@SuppressWarnings("unused") // bad lint
	public static ResourceLocation getLocation(@This Music self) {
		#if MC_1_19_OR_OLDER
		return self.event.location;
		#else
		return self.event.value().location;
		#endif
	}
}
