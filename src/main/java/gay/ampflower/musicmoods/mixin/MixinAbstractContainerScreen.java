/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import gay.ampflower.musicmoods.Config;
import gay.ampflower.musicmoods.client.MusicHandler;
import gay.ampflower.musicmoods.util.JukeboxUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
#if MC_1_21_11_OR_OLDER
import net.minecraft.world.inventory.ClickType;
#else
import net.minecraft.world.inventory.ContainerInput;
#endif
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * @author Ampflower
 * @since 0.6.12
 **/
@Mixin(AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {

	@WrapWithCondition(
		method = "mouseClicked",
		at = @At(
			value = "INVOKE",
			#if MC_1_21_11_OR_OLDER
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V",
			#else
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V",
			#endif
			ordinal = 0
		),
		slice = @Slice(from = @At(
			value = "FIELD",
			#if MC_1_21_11_OR_OLDER
			target = "Lnet/minecraft/world/inventory/ClickType;PICKUP:Lnet/minecraft/world/inventory/ClickType;"
			#else
			target = "Lnet/minecraft/world/inventory/ContainerInput;PICKUP:Lnet/minecraft/world/inventory/ContainerInput;"
			#endif
		))
	)
	private static boolean musicmoods$playDisc(
		final AbstractContainerScreen<?> self,
		final Slot slot,
		final int index,
		final int button,
		#if MC_1_21_11_OR_OLDER
		final ClickType clickType
		#else
		final ContainerInput clickType
		#endif
	) {
		final var level = Minecraft.getInstance().level;

		// General fast path
		if (level == null
			|| !Config.rightClickToPlay
			#if MC_1_21_11_OR_OLDER
			|| clickType != ClickType.PICKUP
			#else
			||clickType != ContainerInput.PICKUP
			#endif
			|| button != 1
			// Ensure we're one of either of these.
			// We don't really want to mess with normal inventory interactions.
			|| !isScreenPlayerInventory(self)
		) {
			return true;
		}

		#if MC_1_20_5_OR_OLDER
		final var optionalSong = JukeboxUtil.toStereoElseMono(slot.getItem());
		#else
		final var optionalSong = JukeboxUtil.toStereoElseMono(level.registryAccess(), slot.getItem());
		#endif

		if (optionalSong.isEmpty()) {
			return true;
		}

		final var musicManager = Minecraft.getInstance().getMusicManager();
		final var musicHandler = (MusicHandler) musicManager;

		if (musicHandler.moods$isCurrentlyPlaying(optionalSong.get())) {
			musicManager.stopPlaying();
		} else {
			musicHandler.moods$intrudeJukeboxTrack(optionalSong.get());
		}

		return false;
	}

	@Unique
	private static boolean isScreenPlayerInventory(AbstractContainerScreen<?> screen) {
		return screen instanceof CreativeModeInventoryScreen
			|| screen instanceof InventoryScreen
			|| screen instanceof HorseInventoryScreen;
	}
}
