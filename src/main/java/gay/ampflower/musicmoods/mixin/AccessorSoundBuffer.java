package gay.ampflower.musicmoods.mixin;

import com.mojang.blaze3d.audio.SoundBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.sound.sampled.AudioFormat;

/**
 * @author Ampflower
 * @since 0.7.0
 **/
@Mixin(SoundBuffer.class)
public interface AccessorSoundBuffer {
	@Accessor
	AudioFormat getFormat();
}
