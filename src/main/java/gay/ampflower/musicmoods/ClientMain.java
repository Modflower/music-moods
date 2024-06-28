/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods;// Created 2023-12-01T02:08:34

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;

/**
 * @author Ampflower
 * @since 0.0.0
 **/
public class ClientMain implements ClientModInitializer {
	public static boolean isModMenuPresent = FabricLoader.getInstance().isModLoaded("modmenu");

	@Override
	public void onInitializeClient() {
		try {
			Config.read();
		} catch (IOException ioe) {
			throw new RuntimeException("Unable to load Music Moods Config", ioe);
		}
	}
}
