package gay.ampflower.musicmoods.util;

import gay.ampflower.musicmoods.Config;

#if FABRIC
import net.fabricmc.loader.api.FabricLoader;
#endif

#if FORGE
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
#endif

#if NEOFORGE
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
#endif

import java.io.IOException;

/**
 * @author Ampflower
 * @since 0.7
 **/
public final class Platform {
	#if FABRIC
	public static boolean isModLoaded(String mod) {
		return FabricLoader.instance.isModLoaded(mod);
	}

	public static boolean isModMixinable(String mod) {
		return isModLoaded(mod);
	}
	#else
	public static boolean isModLoaded(String mod) {
		return ModList.get().isLoaded(mod);
	}

	#if NEOFORGE_1_21_9_OR_NEWER
	public static boolean isModMixinable(String mod) {
		return FMLLoader.getCurrent().getLoadingModList().mods.stream().anyMatch(info -> mod.equals(info.modId));
	}
	#else
	public static boolean isModMixinable(String mod) {
		return FMLLoader.getLoadingModList().mods.stream().anyMatch(info -> mod.equals(info.modId));
	}
	#endif
	#endif

	public static boolean isModNotLoaded(String mod) {
		return !isModLoaded(mod);
	}

	public static void init() {
		try {
			Config.read();
		} catch (IOException ioe) {
			throw new RuntimeException("Unable to load Music Moods Config", ioe);
		}
	}
}
