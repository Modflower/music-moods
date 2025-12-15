package gay.ampflower.musicmoods.mixin.compat.timm;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import gay.ampflower.musicmoods.Config;
import gay.ampflower.musicmoods.client.MusicHandler;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

#if MC_1_21_4_OR_NEWER && !MC_1_21_11_OR_NEWER
#define MUSIC_INFO
import net.minecraft.client.sounds.MusicInfo;
#else
import net.minecraft.sounds.Music;
#endif

/**
 * @author Ampflower
 * @since 0.7
 **/
@Pseudo
@Debug(export = true)
@Mixin(value = MusicManager.class, priority = 1500)
public final class MixinMusicManager {
	@TargetHandler(
		mixin = "com.github.charlyb01.timm.client.mixin.MusicTrackerMixin",
		name = "onTick(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
	)
	@Inject(
		method = "@MixinSquared:Handler",
		at = @At(
			value = "INVOKE",
			target = "Lcom/github/charlyb01/timm/config/ModConfig;get()Lcom/github/charlyb01/timm/config/ModConfig;",
			shift = At.Shift.BEFORE
		),
		require = 0,
		cancellable = true
	)
	private void disableFader(CallbackInfo parentCi, CallbackInfo ci) {
		if (Config.timm$disableFade) {
			ci.cancel();
		}
	}

	@TargetHandler(
		mixin = "com.github.charlyb01.timm.client.mixin.MusicTrackerMixin",
		name = "playStructureMusic()V"
	)
	@WrapWithCondition(
		method = "@MixinSquared:Handler",
		at = @At(
			value = "INVOKE",
			#if MUSIC_INFO
			target = "Lnet/minecraft/client/sounds/MusicManager;startPlaying(Lnet/minecraft/client/sounds/MusicInfo;)V"
			#else
			target = "Lnet/minecraft/client/sounds/MusicManager;startPlaying(Lnet/minecraft/sounds/Music;)V"
			#endif
		),
		require = 0
	)
	private boolean intrudePlay(
		final @Coerce MusicHandler self,
		final #if(MUSIC_INFO) MusicInfo #else Music #endif music
	) {
		if (Config.timm$intrudeStructureMusic) {
			self.moods$intrudeMusic(music);
			return false;
		}
		return true;
	}
}
