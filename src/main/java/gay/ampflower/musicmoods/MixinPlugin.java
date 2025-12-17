package gay.ampflower.musicmoods;

#if OLD_FORGE

import com.bawnorton.mixinsquared.MixinSquaredBootstrap;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;
#endif

import gay.ampflower.musicmoods.util.Platform;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * @author Ampflower
 * @since 0.7
 **/
public class MixinPlugin implements IMixinConfigPlugin {
	private static final String compat = "gay.ampflower.musicmoods.mixin.compat.";

	@Override
	public void onLoad(final String mixinPackage) {
		#if OLD_FORGE
		MixinSquaredBootstrap.init();
		MixinExtrasBootstrap.init();
		#endif
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
		if (mixinClassName.startsWith(compat)) {
			final var mod = mixinClassName.substring(compat.length(), mixinClassName.lastIndexOf('.'));
			return Platform.isModMixinable(mod);
		}
		return true;
	}

	@Override
	public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		#if OLD_FORGE
		MixinSquaredBootstrap.reOrderExtensions();
		#endif
		return null;
	}

	@Override
	public void preApply(
		final String targetClassName,
		final ClassNode targetClass,
		final String mixinClassName,
		final IMixinInfo mixinInfo
	) {

	}

	@Override
	public void postApply(
		final String targetClassName,
		final ClassNode targetClass,
		final String mixinClassName,
		final IMixinInfo mixinInfo
	) {

	}
}
