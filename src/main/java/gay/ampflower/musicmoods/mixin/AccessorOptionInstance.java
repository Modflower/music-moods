package gay.ampflower.musicmoods.mixin;

#if MC_1_21_6_OR_NEWER

import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Ampflower
 * @since 0.7
 **/
@Mixin(OptionInstance.class)
public interface AccessorOptionInstance<T> {
	@Accessor
	OptionInstance.TooltipSupplier<T> getTooltip();
}

#endif
