/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.client.sound;

import gay.ampflower.musicmoods.Mint;
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
	private boolean toRelative;
	private Vec3 delta, dest;
	private final float duration = 20.f;

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

		if (dest != null && delta != null) {
			final var scale = delta.scale(Math.min(this.tickDelta, 1.f));
			this.x += scale.x();
			this.y += scale.y();
			this.z += scale.z();

			if (dest.distanceToSqr(this.x, this.y, this.z) < Math.max(delta.lengthSqr(), 0.1)) {
				if (toRelative) {
					toRelative(null, null);
				} else {
					this.x = dest.x();
					this.y = dest.y();
					this.z = dest.z();
				}

				dest = null;
				delta = null;
			}
		}
	}

	public void centerOnPlayer(Vec3 camera, Vec2 rotation) {
		if (this.relative && this.x == 0 && this.y == 0 && this.z == 0) {
			return;
		}
		toRelative(camera, rotation);
		startTransition(Vec3.ZERO);
	}

	public void centerOnOrigin(Vec3 camera, Vec2 rotation) {
		fromRelative(camera, rotation);
		startTransition(origin);
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

	private void startTransition(Vec3 dest) {
		if (dest.equals(this.dest)) {
			return;
		}

		if (dest.x() == this.x && dest.y() == this.y && dest.z() == this.z) {
			return;
		}

		// FIXME: this realistically should be a curve that is biased towards the front of the player.
		this.dest = dest;
		this.delta = dest.subtract(this.x, this.y, this.z).scale(1 / duration);
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
