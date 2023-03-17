/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package net.kjp12.musicmoods.client;// Created 2023-11-01T07:49:11

import net.minecraft.client.resources.sounds.Sound;

/**
 * @author Ampflower
 * @since 0.0.0
 **/
public interface WeighedSoundEventsQuery {
	boolean contains(Sound sound);
}
