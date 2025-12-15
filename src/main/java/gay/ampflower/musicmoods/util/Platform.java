package gay.ampflower.musicmoods.util;

import gay.ampflower.musicmoods.Config;

#if FABRIC
import net.fabricmc.loader.api.FabricLoader;
#endif

#if FORGE
import net.minecraftforge.fml.ModList;
#endif

#if NEOFORGE
import net.neoforged.fml.ModList;
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
	#else
	public static boolean isModLoaded(String mod) {
		return ModList.get().isLoaded(mod);
	}
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
