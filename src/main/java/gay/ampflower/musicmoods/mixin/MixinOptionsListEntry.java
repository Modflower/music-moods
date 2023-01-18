/* Copyright 2023 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.mixin;// Created 2023-17-01T20:06:21

import gay.ampflower.musicmoods.Config;
import gay.ampflower.musicmoods.client.WidgetAttachment;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author Ampflower
 * @since 0.1.0
 **/
@Mixin(targets = "net.minecraft.client.gui.components.OptionsList$Entry")
public class MixinOptionsListEntry {
	@Shadow
	@Final
	Map<OptionInstance<?>, AbstractWidget> options;

	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;copyOf(Ljava/util/Collection;)Lcom/google/common/collect/ImmutableList;"))
	private Collection<AbstractWidget> musicmoods$modifyListBeforeSet(Collection<AbstractWidget> collection) {
		if (!Config.injectUiComponents || collection.size() > 2 || collection.isEmpty()) {
			return collection;
		}

		final var list = new ArrayList<AbstractWidget>(collection.size() * 2);

		final var itr = this.options.entrySet().iterator();

		{
			final var current = itr.next();
			final var value = current.getValue();

			final var factory = WidgetAttachment.binding.get(current.getKey());
			if (factory != null)
				list.add(factory.createWidget(value, true));

			list.add(value);
		}

		if (itr.hasNext()) {
			final var current = itr.next();
			final var value = current.getValue();

			list.add(value);

			final var factory = WidgetAttachment.binding.get(current.getKey());
			if (factory != null)
				list.add(factory.createWidget(value, false));
		}

		return list;
	}
}
