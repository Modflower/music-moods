package gay.ampflower.musicmoods.mixin;

import gay.ampflower.musicmoods.Config;
import gay.ampflower.musicmoods.client.ConfigurationScreen;
import net.minecraft.client.gui.screens.Screen;
#if MC_1_20_5_OR_OLDER
import net.minecraft.client.gui.screens.OptionsScreen;
#else
import net.minecraft.client.gui.screens.options.OptionsScreen;
#endif
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
#if MC_1_19_OR_OLDER
import org.spongepowered.asm.mixin.injection.ModifyArg;
#endif
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Ampflower
 * @since 0.7
 **/
@Mixin(OptionsScreen.class)
public abstract class MixinOptionsScreen extends Screen {
	protected MixinOptionsScreen(final Component component) {
		super(component);
	}

	#if MC_1_19_OR_OLDER
	@ModifyArg(
		method = "method_19829",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"
		),
		require = 0
	)
	private Screen replaceScreen(Screen original) {
		return new ConfigurationScreen(this);
	}
	#elif FORGE
	// This for some reason only works on Forge..???
	@Inject(
		method = "*",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/SoundOptionsScreen;<init>(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/Options;)V"
		),
		cancellable = true
	)
	private void wrapScreen(CallbackInfoReturnable<Screen> cir) {
		if (!Config.injectUiComponents) {
			return;
		}
		cir.returnValue = new ConfigurationScreen(this);
	}
	#else
	// This of course, doesn't get remapped by Arch Loom on Forge...???
	@Inject(
		method = "method_19829",
		at = @At("HEAD"),
		allow = 1,
		cancellable = true
	)
	private void wrapScreen(CallbackInfoReturnable<Screen> cir) {
		if (!Config.injectUiComponents) {
			return;
		}
		cir.returnValue = new ConfigurationScreen(this);
	}
	#endif
}
