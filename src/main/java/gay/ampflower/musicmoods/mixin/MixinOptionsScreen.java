package gay.ampflower.musicmoods.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import gay.ampflower.musicmoods.Config;
import gay.ampflower.musicmoods.client.ConfigurationScreen;
import net.minecraft.client.gui.screens.Screen;
#if MC_1_20_5_OR_OLDER
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
#else
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
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
	#else
	// Well, intermediary's dead now.
	// This also is properly universal too.
	@Inject(
		method = "*",
		at = @At("MIXINEXTRAS:EXPRESSION"),
		allow = 1,
		cancellable = true
	)
	@Definition(id = "SoundOptionsScreen", type = SoundOptionsScreen.class)
	@Definition(
		#if MC_1_20_5_OR_OLDER
		field = "Lnet/minecraft/client/gui/screens/OptionsScreen;options:Lnet/minecraft/client/Options;",
		#else
		field = "Lnet/minecraft/client/gui/screens/options/OptionsScreen;options:Lnet/minecraft/client/Options;",
		#endif
		id = "options"
	)

	@Expression("new SoundOptionsScreen(this, this.options)")
	private void wrapScreen(CallbackInfoReturnable<Screen> cir) {
		if (!Config.injectUiComponents) {
			return;
		}
		cir.returnValue = new ConfigurationScreen(this);
	}
	#endif
}
