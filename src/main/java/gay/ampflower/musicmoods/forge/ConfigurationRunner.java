package gay.ampflower.musicmoods.forge;

#if FORGE

import gay.ampflower.musicmoods.ClientMain;
import gay.ampflower.musicmoods.client.ConfigurationScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * @author Ampflower
 * @since 0.7
 **/
public class ConfigurationRunner implements DistExecutor.SafeRunnable {
	@Override
	public void run() {
		ModLoadingContext.get().activeContainer.registerExtensionPoint(
			ConfigScreenHandler.ConfigScreenFactory.class,
			() -> new ConfigScreenHandler.ConfigScreenFactory(
				(minecraft, parent) -> new ConfigurationScreen(parent)
			)
		);

		new ClientMain().onInitializeClient();
	}
}

#endif
