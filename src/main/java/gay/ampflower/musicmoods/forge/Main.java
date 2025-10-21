package gay.ampflower.musicmoods.forge;

#if NEOFORGE || FORGE

#if NEOFORGE

import gay.ampflower.musicmoods.ClientMain;
import gay.ampflower.musicmoods.client.ConfigurationScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

#endif

#if FORGE

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

#endif

/**
 * @author Ampflower
 * @since 0.7
 */
#if NEOFORGE
@Mod(value = "music_moods", dist = Dist.CLIENT)
#elif FORGE
@Mod("music_moods")
#endif
public class Main {

	#if NEOFORGE
	public Main(ModContainer container) {
		container.registerExtensionPoint(IConfigScreenFactory.class, (self, parent) -> new ConfigurationScreen(parent));
		new ClientMain().onInitializeClient();
	}
	#endif

	#if FORGE
	public Main() {
		// Reflection because I am not entertaining a broken resolver.
		// Have a functional sided execution implementation.
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> (DistExecutor.SafeRunnable) Class
			.forName("gay.ampflower.musicmoods.forge.ConfigurationRunner")
			.getConstructor()
			.newInstance()
		);
	}
	#endif
}

#endif
