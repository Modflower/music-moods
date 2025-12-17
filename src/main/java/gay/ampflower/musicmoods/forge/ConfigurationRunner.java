package gay.ampflower.musicmoods.forge;

#if FORGE

import gay.ampflower.musicmoods.client.ConfigurationScreen;
import gay.ampflower.musicmoods.util.Platform;
#if MC_1_16_4_OR_OLDER
import net.minecraftforge.fml.ExtensionPoint;
#elif MC_1_17_OR_OLDER
import net.minecraftforge.fmlclient.ConfigGuiHandler;
#elif MC_1_18_OR_OLDER
import net.minecraftforge.client.ConfigGuiHandler;
#else
import net.minecraftforge.client.ConfigScreenHandler;
#endif

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * @author Ampflower
 * @since 0.7
 **/
public class ConfigurationRunner implements DistExecutor.SafeRunnable {
	@Override
	public void run() {

		#if MC_1_16_4_OR_OLDER
		ModLoadingContext.get().activeContainer.registerExtensionPoint(
			ExtensionPoint.CONFIGGUIFACTORY,
			() -> (minecraft, parent) -> new ConfigurationScreen(parent)
		);
		#elif MC_1_18_OR_OLDER
		ModLoadingContext.get().activeContainer.registerExtensionPoint(
			ConfigGuiHandler.ConfigGuiFactory.class,
			() -> new ConfigGuiHandler.ConfigGuiFactory(
				(minecraft, parent) -> new ConfigurationScreen(parent)
			)
		);
		#else
		ModLoadingContext.get().activeContainer.registerExtensionPoint(
			ConfigScreenHandler.ConfigScreenFactory.class,
			() -> new ConfigScreenHandler.ConfigScreenFactory(
				(minecraft, parent) -> new ConfigurationScreen(parent)
			)
		);
		#endif

		Platform.init();
	}
}

#endif
