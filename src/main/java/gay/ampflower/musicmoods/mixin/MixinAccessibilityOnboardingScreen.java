package gay.ampflower.musicmoods.mixin;

#if MC_26_1_OR_NEWER

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import gay.ampflower.musicmoods.Config;
import gay.ampflower.musicmoods.client.ConfigurationScreen;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.Screen;
#if MC_1_20_5_OR_OLDER
import net.minecraft.client.gui.screens.SoundOptionsScreen;
#else
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * @author Ampflower
 * @since 0.7
 **/
@Mixin(AccessibilityOnboardingScreen.class)
public class MixinAccessibilityOnboardingScreen {
	@ModifyArg(
		method = "*",
		at = @At("MIXINEXTRAS:EXPRESSION"),
		allow = 1
	)
	@Definition(id = "SoundOptionsScreen", type = SoundOptionsScreen.class)
	@Definition(
		field = "Lnet/minecraft/client/gui/screens/AccessibilityOnboardingScreen;options:Lnet/minecraft/client/Options;",
		id = "options"
	)
	@Definition(
		id = "closeAndSetScreen",
		method = "Lnet/minecraft/client/gui/screens/AccessibilityOnboardingScreen;closeAndSetScreen(Lnet/minecraft/client/gui/screens/Screen;)V"
	)
	@Expression("this.closeAndSetScreen(new SoundOptionsScreen(this, this.options))")
	private Screen wrapScreen(Screen original) {
		if (!Config.injectUiComponents) {
			return original;
		}
		return new ConfigurationScreen((Screen) (Object) this);
	}

}

#endif
