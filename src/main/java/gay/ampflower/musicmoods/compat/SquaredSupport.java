package gay.ampflower.musicmoods.compat;

import com.bawnorton.mixinsquared.TargetHandler;
import org.objectweb.asm.tree.AnnotationNode;

/**
 * @author Ampflower
 * @since 0.7
 **/
final class SquaredSupport {
	private static final String targetHandlerDesc = TargetHandler.class.descriptorString();

	public static AnnotationNode targetHandler(
		final String mixin,
		final String name
	) {
		final AnnotationNode target = new AnnotationNode(targetHandlerDesc);
		target.visit("mixin", mixin);
		target.visit("name", name);
		return target;
	}

	public static AnnotationNode targetHandler(
		final String mixin,
		final String name,
		final String prefix
	) {
		final AnnotationNode target = targetHandler(mixin, name);
		target.visit("prefix", prefix);
		return target;
	}
}
