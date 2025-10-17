/*
 * Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package gay.ampflower.musicmoods.extensions.net.minecraft.resources.ResourceLocation;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

#if MC_1_19_OR_OLDER

import net.minecraft.resources.ResourceLocation;

/**
 * @author Ampflower
 * @since 0.7
 **/
@Extension
public final class ExtResourceLocation {
	public static ResourceLocation withPath(@This ResourceLocation self, final String path) {
		return new ResourceLocation(self.namespace, path);
	}

	public static ResourceLocation withPrefix(@This ResourceLocation self, final String prefix) {
		return withPath(self, prefix + self.getPath());
	}
}

#endif
