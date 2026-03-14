package gay.ampflower.musicmoods.mixin;

import gay.ampflower.musicmoods.debug.Debugger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.WinScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Ampflower
 * @since 0.7.0
 **/
@Mixin(value = {Minecraft.class, WinScreen.class}, priority = 500)
public class MixinMusicManagerTicker {
	@Inject(
		method = "*",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/sounds/MusicManager;tick()V"
		)
	)
	private void beforeMusicManagerTick(CallbackInfo ci) {
		Debugger.musicManager.trackTick();
	}

	@Inject(
		method = "*",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/sounds/MusicManager;tick()V",
			shift = At.Shift.AFTER
		)
	)
	private void afterMusicManagerTick(CallbackInfo ci) {
		Debugger.musicManager.test();
	}
}
