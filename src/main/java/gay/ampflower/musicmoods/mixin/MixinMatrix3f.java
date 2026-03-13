package gay.ampflower.musicmoods.mixin;

#if MC_1_19_OR_OLDER

import com.mojang.math.Matrix3f;
import gay.ampflower.musicmoods.util.ExtMatrix3f;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author Ampflower
 * @since 0.7.0
 **/
@Mixin(Matrix3f.class)
public class MixinMatrix3f implements ExtMatrix3f {
	@Shadow protected float m00;
	@Shadow protected float m01;
	@Shadow protected float m02;
	@Shadow protected float m10;
	@Shadow protected float m11;
	@Shadow protected float m12;
	@Shadow protected float m20;
	@Shadow protected float m21;
	@Shadow protected float m22;

	// Now, it may seem strange to need this, but:
	// Mojang never added the ability to arbitrarily initialize matrices.
	@Override
	public Matrix3f musicmoods$set(
		final float m00, final float m01, final float m02,
		final float m10, final float m11, final float m12,
		final float m20, final float m21, final float m22
	) {
		this.m00 = m00;
		this.m01 = m01;
		this.m02 = m02;
		this.m10 = m10;
		this.m11 = m11;
		this.m12 = m12;
		this.m20 = m20;
		this.m21 = m21;
		this.m22 = m22;
		return (Matrix3f)(Object)this;
	}

	@Override
	public Vec3 musicmoods$transform(final Vec3 vec) {
		final double x = Math.fma(m00, vec.x, Math.fma(m01, vec.y, m02 * vec.z));
		final double y = Math.fma(m10, vec.x, Math.fma(m11, vec.y, m12 * vec.z));
		final double z = Math.fma(m20, vec.x, Math.fma(m21, vec.y, m22 * vec.z));
		return new Vec3(x, y, z);
	}
}

#endif
