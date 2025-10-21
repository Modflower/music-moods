/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods;// Created 2023-12-01T02:08:34

#if FABRIC

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
#endif

#if NEOFORGE

import gay.ampflower.musicmoods.client.ConfigurationScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
#endif

#if FORGE

import gay.ampflower.musicmoods.client.ConfigurationScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

#endif

import java.io.IOException;

/**
 * @author Ampflower
 * @since 0.0.0
 **/
#if NEOFORGE
@Mod(value = "music_moods", dist = Dist.CLIENT)
#elif FORGE
@Mod("music_moods")
@OnlyIn(Dist.CLIENT)
#endif
public class ClientMain #if(FABRIC) implements ClientModInitializer #endif {
	public static boolean isModMenuPresent;

	static {
		#if FABRIC
		isModMenuPresent = FabricLoader.getInstance().isModLoaded("modmenu");
		#else
		// Neoforge and Forge always has a mod menu.
		isModMenuPresent = true;
		#endif
	}

	#if NEOFORGE
	public ClientMain(ModContainer container) {
		container.registerExtensionPoint(IConfigScreenFactory.class, (self, parent) -> new ConfigurationScreen(parent));
		onInitializeClient();
	}
	#endif

	#if FORGE
	public ClientMain() {
		ModLoadingContext.get().activeContainer.registerExtensionPoint(
			ConfigScreenHandler.ConfigScreenFactory.class,
			() -> new ConfigScreenHandler.ConfigScreenFactory(
				(minecraft, parent) -> new ConfigurationScreen(parent)
			)
		);
		onInitializeClient();
	}
	#endif

	public void onInitializeClient() {
		try {
			Config.read();
		} catch (IOException ioe) {
			throw new RuntimeException("Unable to load Music Moods Config", ioe);
		}
	}
}
