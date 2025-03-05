/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec2;

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

	public static float square(float base) {
		return base * base;
	}

	public static double square(double base) {
		return base * base;
	}

	public static Vec2 cameraToRotationVector(Camera camera) {
		return new Vec2(camera.getXRot(), camera.getYRot());
	}
}
