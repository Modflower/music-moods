package gay.ampflower.musicmoods.util;

#if MC_1_19_OR_OLDER

import com.mojang.math.Matrix3f;
import net.minecraft.world.phys.Vec3;

/**
 * @author Ampflower
 * @since 0.7.0
 **/
public interface ExtMatrix3f {

	/**
	 * Directly sets the matrix to the provided values.
	 */
	Matrix3f musicmoods$set(
		final float m00, final float m01, final float m02,
		final float m10, final float m11, final float m12,
		final float m20, final float m21, final float m22
	);

	/**
	 * Applies the matrix transformations to the given vector.
	 */
	Vec3 musicmoods$transform(final Vec3 vector);
}

#endif
