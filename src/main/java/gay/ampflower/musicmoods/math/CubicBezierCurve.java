package gay.ampflower.musicmoods.math;

import net.minecraft.world.phys.Vec3;

/**
 * @author Ampflower
 * @since 0.7.0
 **/
public record CubicBezierCurve(Vec3 point1, Vec3 point2, Vec3 point3, Vec3 point4) implements Lerpable<Vec3> {
	public Vec3 lerp(final double delta) {
		return BezierCurves.lerp(point1, point2, point3, point4, delta);
	}
}
