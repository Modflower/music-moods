package gay.ampflower.musicmoods.compat;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.transformer.ClassInfo;

/**
 * @author Ampflower
 * @since 0.7
 **/
public interface MixinClassAdjuster {
	default boolean apply(
		final String targetClassName,
		final ClassInfo targetClassInfo,
		final ClassNode targetClassNode,
		final String mixinClassName,
		final ClassInfo mixinClassInfo,
		final ClassNode mixinClassNode
	) {
		return this.apply(targetClassName, mixinClassName, mixinClassInfo, mixinClassNode);
	}

	/**
	 * @return Whether the mixin was adjusted. Taints the class.
	 */
	default boolean apply(
		final String targetClassName,
		final String mixinClassName,
		final ClassInfo mixinClassInfo,
		final ClassNode mixinClassNode
	) {
		return false;
	}
}
