/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.client.sound;

import gay.ampflower.musicmoods.Mint;
import gay.ampflower.musicmoods.math.CubicBezierCurve;
#if MC_1_19_OR_NEWER
import net.minecraft.client.resources.sounds.SoundInstance;
#endif
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * @author Ampflower
 * @since 0.5
 **/
public class RecordSoundInstance extends FadeableSoundInstance implements Relativeable {

	private final Vec3 origin;

	private boolean relativeDirty;
	private CubicBezierCurve curve;
	private float ticks, div;

	public RecordSoundInstance(final SoundEvent soundEvent, final BlockPos origin) {
		this(soundEvent, origin.getX() + .5D, origin.getY() + .5D, origin.getZ() + .5D);
	}

	public RecordSoundInstance(final SoundEvent soundEvent, final double x, final double y, final double z) {
		#if MC_1_18_OR_OLDER
		super(soundEvent, SoundSource.RECORDS);
		#else
		super(soundEvent, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
		#endif

		this.origin = new Vec3(x, y, z);

		this.volume = 4.F;
		this.maxVolume = 4.F;
		this.pitch = 1.F;

		this.x = x;
		this.y = y;
		this.z = z;

		this.attenuation = Attenuation.LINEAR;
	}

	@Override
	public void tick() {
		super.tick();

		if (this.curve != null) {
			final double delta = (this.ticks += this.tickDelta) * div;

			this.setPosition(curve.lerp(delta));

			if (delta >= 1.d) {
				this.curve = null;
			}
		}
	}

	public void centerOnPlayer(Vec3 camera, Vec2 rotation) {
		if (this.relative && this.isDestinationSame(Vec3.ZERO)) {
			return;
		}
		this.ticks = 0.f;
		toRelative(camera, rotation);

		// AL ~ x = back, y = right, z = up
		final var current = this.getPosition();
		final double dist = current.distanceTo(Vec3.ZERO);

		this.setDeltaMultiplier(dist);
		this.curve = new CubicBezierCurve(
			current,
			Mint.lerp(current, Vec3.ZERO, 0.5d),
			new Vec3(
				0,
				Math.copySign(dist, current.y) * 0.45d,
				current.z * 0.25d
			),
			Vec3.ZERO
		);
	}

	public void centerOnOrigin(Vec3 camera, Vec2 rotation) {
		if (!this.relative && this.isDestinationSame(this.origin)) {
			return;
		}
		this.ticks = 0.f;
		fromRelative(camera, rotation);

		final var current = this.getPosition();

		// if it's over 5 blocks away, the transition is unlikely to be noticed
		// Although we could perhaps derive the quadratic curve to make it
		// bounce back more seamlessly, if this is a problem.
		if (camera.distanceToSqr(current) > 25.d) {
			this.curve = new CubicBezierCurve(
				current,
				Mint.lerp(current, this.origin, 0.15d),
				Mint.lerp(current, this.origin, 0.85d),
				this.origin
			);
			return;
		}

		// MC ~ x = forward, y = up, z = left
		final var local = Mint.globalToLocal(camera, rotation, this.origin);
		final double dist = current.distanceTo(this.origin);

		this.setDeltaMultiplier(dist);
		this.curve = new CubicBezierCurve(
			current,
			Mint.localToGlobal(camera, rotation, new Vec3(
				Math.copySign(dist, local.x) * 0.45d,
				local.y * -0.25d,
				0
			)),
			Mint.lerp(current, this.origin, 0.5d),
			this.origin
		);
	}

	private void fromRelative(Vec3 camera, Vec2 rotation) {
		if (this.relative) {
			final var local = Mint.alToLocal(this.x, this.y, this.z);
			this.setPosition(Mint.localToGlobal(camera, rotation, local));

			this.relativeDirty = true;
			this.relative = false;
		}
	}

	private void toRelative(Vec3 player, Vec2 rotation) {
		if (!this.relative) {
			final var local = Mint.globalToLocal(player, rotation, this.getPosition());
			this.setPosition(Mint.localToAl(local));

			this.relativeDirty = true;
			this.relative = true;
		}
	}

	private void setDeltaMultiplier(final double distance) {
		this.div = (float)(1/(20.d/32.d * distance));
	}

	private boolean isDestinationSame(Vec3 dest) {
		if (dest.x() == this.x && dest.y() == this.y && dest.z() == this.z) {
			return true;
		}

		if (this.curve == null) {
			return false;
		}

		return this.curve.point4().equals(dest);
	}

	private void setPosition(Vec3 vec3) {
		this.x = vec3.x();
		this.y = vec3.y();
		this.z = vec3.z();
	}

	private Vec3 getPosition() {
		return new Vec3(this.x, this.y, this.z);
	}

	@Override
	public boolean isRelativeDirty() {
		return relativeDirty;
	}
}
