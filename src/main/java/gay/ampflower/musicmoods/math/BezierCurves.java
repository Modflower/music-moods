package gay.ampflower.musicmoods.math;

import gay.ampflower.musicmoods.Mint;
import net.minecraft.world.phys.Vec3;

/**
 * @author Ampflower
 * @since 0.7.0
 **/
public final class BezierCurves {
	public static Vec3 lerp(
		final Vec3 point1,
		final Vec3 point2,
		final Vec3 point3,
		final Vec3 point4,
		final double delta
	) {
		if (delta <= 0.d) {
			return point1;
		}
		if (delta >= 1.d) {
			return point4;
		}

		final Vec3 q1 = Mint.lerp(point1, point2, delta);
		final Vec3 q2 = Mint.lerp(point2, point3, delta);
		final Vec3 q3 = Mint.lerp(point3, point4, delta);

		final Vec3 l1 = Mint.lerp(q1, q2, delta);
		final Vec3 l2 = Mint.lerp(q2, q3, delta);

		return Mint.lerp(l1, l2, delta);
	}

	public static Vec3 lerp(
		final Vec3 point1,
		final Vec3 point2,
		final Vec3 point3,
		final double delta
	) {
		if (delta <= 0.d) {
			return point1;
		}
		if (delta >= 1.d) {
			return point3;
		}

		final Vec3 l1 = Mint.lerp(point1, point2, delta);
		final Vec3 l2 = Mint.lerp(point2, point3, delta);

		return Mint.lerp(l1, l2, delta);
	}
}
