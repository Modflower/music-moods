/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods;

import net.minecraft.client.Camera;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
#if MC_1_19_OR_OLDER
import com.mojang.math.Matrix3f;
import gay.ampflower.musicmoods.util.ExtMatrix3f;
#else
import org.joml.Matrix3d;
import org.joml.Vector3d;
#endif

/**
 * @author Ampflower
 * @since 0.5
 **/
public final class Mint {

	public static final double WHOLE_CIRCLE_DEG = 360;
	public static final double HALF_CIRCLE_DEG = 180;

	public static final double TAU = Math.PI * 2;
	public static final double HALF_PI = Math.PI / 2;
	public static final double DEG_TO_RAD = Math.PI / 180;
	public static final double RAD_TO_DEG = 180 / Math.PI;

	public static double wrapRadiansPositive(double rad) {
		rad %= Mint.TAU;
		if (rad < 0) {
			rad += Mint.TAU;
		}
		return rad;
	}

	public static double wrapRadians(double rad) {
		rad %= Mint.TAU;
		if (rad < -Math.PI) {
			rad += Mint.TAU;
		}
		if (rad > Math.PI) {
			rad -= Mint.TAU;
		}
		return rad;
	}

	public static double wrapDegreesPositive(double deg) {
		deg %= WHOLE_CIRCLE_DEG;
		if (deg < 0) {
			deg += WHOLE_CIRCLE_DEG;
		}
		return deg;
	}

	public static double wrapDegrees(double deg) {
		deg %= WHOLE_CIRCLE_DEG;
		if (deg < -HALF_CIRCLE_DEG) {
			deg += WHOLE_CIRCLE_DEG;
		}
		if (deg > HALF_CIRCLE_DEG) {
			deg += HALF_CIRCLE_DEG;
		}
		return deg;
	}

	public static int wrapPositive(int value, int mod) {
		value %= mod;
		if (value < 0) {
			value += mod;
		}
		return value;
	}

	public static float square(float base) {
		return base * base;
	}

	public static double square(double base) {
		return base * base;
	}

	public static Vec2 cameraToRotationVector(Camera camera) {
		#if MC_1_21_11_OR_NEWER
		return new Vec2(camera.xRot(), camera.yRot());
		#else
		return new Vec2(camera.getXRot(), camera.getYRot());
		#endif
	}

	public static Vec3 cameraToPosition(Camera camera) {
		#if MC_1_21_11_OR_NEWER
		return camera.position();
		#else
		return camera.getPosition();
		#endif
	}

	/**
	 * Converts a given local coordinate to a global coordinate, using the given camera as an anchor.
	 *
	 * Note: Mojang and OpenAL calls local coordinates 'relative'.
	 *
	 * @param anchor The camera, providing the anchor position and rotation.
	 * @param local The local coordinate position.
	 * @return The global position in the world.
	 * @since 0.7.0
	 * */
	public static Vec3 localToGlobal(final Camera anchor, final Vec3 local) {
		return localToGlobal(cameraToPosition(anchor), cameraToRotationVector(anchor), local);
	}

	/**
	 * Converts a given local coordinate to a global coordinate, using a given anchor coordinate and rotation.
	 *
	 * You can use {@link Vec3#ZERO} as the anchor to make it local to relative instead.
	 *
	 * Note: Mojang and OpenAL calls local coordinates 'relative'.
	 *
	 * @param anchor The anchor in the global coordinate space. May be {@link Vec3#ZERO} for relative.
	 * @param rotation The pitch and yaw, stored as a vector.
	 * @param local The local coordinate position.
	 * @return The global position in the world.
	 * @since 0.7.0
	 */
	// Note: x = forwards, y = up, z = left
	public static Vec3 localToGlobal(final Vec3 anchor, final Vec2 rotation, final Vec3 local) {
		final var perspective = setupPerspective(rotation);

		#if MC_1_19_OR_OLDER
		final Vec3 relative = ((ExtMatrix3f)(Object)perspective).musicmoods$transform(local);
		#else
		final Vector3d relative = perspective.transform(local.x, local.y, local.z, new Vector3d());
		#endif

		return anchor.add(relative.x, relative.y, relative.z);
	}

	/**
	 * Converts a given global coordinate to a local coordinate, using the given camera as an anchor.
	 *
	 * Note: Mojang and OpenAL calls local coordinates 'relative'.
	 *
	 * @param anchor The camera, providing the anchor position and rotation.
	 * @param local The local coordinate position.
	 * @return The global position in the world.
	 * @since 0.7.0
	 * */
	public static Vec3 globalToLocal(final Camera anchor, final Vec3 world) {
		return globalToLocal(cameraToPosition(anchor), cameraToRotationVector(anchor), world);
	}

	/**
	 * Converts a given global coordinate to a local coordinate, using a given anchor coordinate and rotation.
	 *
	 * You can use {@link Vec3#ZERO} as the anchor to make it relative to local instead.
	 *
	 * @param anchor The anchor in the global coordinate space. May be {@link Vec3#ZERO} for relative.
	 * @param rotation The pitch and yaw, stored as a vector.
	 * @param world The global coordinate position.
	 * @return The local position, relative to 0, 0, 0.
	 * @since 0.7.0
	 */
	public static Vec3 globalToLocal(final Vec3 anchor, final Vec2 rotation, final Vec3 world) {
		#if MC_1_19_OR_OLDER
		final var perspective = setupPerspective(rotation);

		perspective.invert();

		return ((ExtMatrix3f)(Object)perspective).musicmoods$transform(world.subtract(anchor));
		#else
		// Inverting the perspective does toLocal, which is what we want.
		// Thank you EtheraelEspeon for pointing this out :3
		final var perspective = setupPerspective(rotation).invert();

		// The matrix isn't a transform matrix,
		// so first convert the global coordinate to relative coordinates.
		final double relativeX = world.x - anchor.x;
		final double relativeY = world.y - anchor.y;
		final double relativeZ = world.z - anchor.z;

		final Vector3d local = perspective.transform(relativeX, relativeY, relativeZ, new Vector3d());

		return new Vec3(local.x, local.y, local.z);
		#endif
	}

	/**
	 * Converts a given rotation pitch/yaw form to a rotation transform matrix that does local -> relative.
	 *
	 * Changes from the vanilla code is that this returns a JOML {@link Matrix3d}.
	 *
	 * If you need to do relative to local, {@link Matrix3d#invert() invert} the matrix.
	 *
	 * @param rotation The pitch & yaw stored as a {@link Vec2}. See {@link #cameraToRotationVector(Camera)}
	 * @return A 3D rotation matrix that takes local coordinates and returns relative coordinates.
	 * @see Vec3#applyLocalCoordinatesToRotation(Vec2, Vec3)
	 * @see net.minecraft.commands.arguments.coordinates.LocalCoordinates#getPosition(CommandSourceStack)
	 */
	private static #if (MC_1_19_OR_OLDER) Matrix3f #else Matrix3d #endif setupPerspective(final Vec2 rotation) {
		// This is still soup to me to understand fully, and to find the JOML equivalent.
		// I presume this is functionally the equivalent of rotating the matrix,
		// given the result in vanilla, was functionally used as a matrix;
		// as demonstrated by being able to stuff the result in JOML's Matrix3d and using its transform method.

		final float yaw = (float)(rotation.y * DEG_TO_RAD + HALF_PI);
		final float p1 = (float)(rotation.x * -DEG_TO_RAD);
		final float p2 = (float)(p1 + HALF_PI);

		final float cosX = Mth.cos(yaw);
		final float sinX = Mth.sin(yaw);
		final float cosY = Mth.cos(p1);
		final float sinY = Mth.sin(p1);
		final float cosZ = Mth.cos(p2);
		final float sinZ = Mth.sin(p2);

		// Produces the matrix rows. This could be made low garbage by omitting the Vec3 outright,
		// however it's very unlikely this code path will be hit often.
		final Vec3 row1 = new Vec3(cosX * cosY, sinY, sinX * cosY);
		final Vec3 row2 = new Vec3(cosX * cosZ, sinZ, sinX * cosZ);
		final Vec3 row3 = row1.cross(row2).scale(-1.0);

		#if MC_1_19_OR_OLDER
		return ((ExtMatrix3f)(Object)new Matrix3f()).musicmoods$set(
			(float)row1.x, (float)row1.y, (float)row1.z,
			(float)row2.x, (float)row2.y, (float)row2.z,
			(float)row3.x, (float)row3.y, (float)row3.z
		);
		#else
		return new Matrix3d(
			row1.x, row1.y, row1.z,
			row2.x, row2.y, row2.z,
			row3.x, row3.y, row3.z
		);
		#endif
	}

	// <editor-fold desc="OpenAL coordinates">

	/** Converts the given local coordinate to an OpenAL coordinate. */
	public static Vec3 localToAl(final double x, final double y, final double z) {
		return new Vec3(-z, -x, y);
	}

	/** Converts the given local coordinate to an OpenAL coordinate. */
	public static Vec3 localToAl(final Vec3 position) {
		return localToAl(position.x, position.y, position.z);
	}

	/** Converts the given OpenAL coordinate to a local coordinate. */
	public static Vec3 alToLocal(final double x, final double y, final double z) {
		return new Vec3(-y, z, -x);
	}

	/** Converts the given OpenAL coordinate to a local coordinate. */
	public static Vec3 alToLocal(final Vec3 position) {
		return alToLocal(position.x, position.y, position.z);
	}
	// </editor-fold>
}
