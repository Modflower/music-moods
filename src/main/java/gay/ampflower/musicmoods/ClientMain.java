/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods;// Created 2023-12-01T02:08:34

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

import java.io.IOException;

/**
 * @author Ampflower
 * @since ${version}
 **/
public class ClientMain implements ClientModInitializer {
	public static boolean isModMenuPresent = QuiltLoader.isModLoaded("modmenu");

	@Override
	public void onInitializeClient(final ModContainer mod) {
		try {
			Config.read();
		} catch (IOException ioe) {
			throw new RuntimeException("Unable to load Music Moods Config", ioe);
		}
	}
}
