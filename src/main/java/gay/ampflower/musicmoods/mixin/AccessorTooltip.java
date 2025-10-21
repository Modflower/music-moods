package gay.ampflower.musicmoods.mixin;

#if MC_1_21_6_OR_NEWER

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Ampflower
 * @since 0.7
 **/
@Mixin(Tooltip.class)
public interface AccessorTooltip {
	@Accessor
	Component getMessage();
}

#endif
