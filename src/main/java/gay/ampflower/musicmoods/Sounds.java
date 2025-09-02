/*
 * Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package gay.ampflower.musicmoods;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author Ampflower
 * @since 0.6.12
 **/
public final class Sounds implements IdentifiableResourceReloadListener {
	private static final Logger logger = LogUtils.getLogger();

	private static final ResourceLocation id = Constants.id("sounds");

	// TODO: Perhaps a config for these?
	//  The server could also broadcast what it's aware of too.
	private static final Map<ResourceLocation, Holder<SoundEvent>> toStereoEvent = new HashMap<>();
	private static final Map<ResourceLocation, ResourceLocation> toStereo = new HashMap<>();

	static {
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new Sounds());
	}

	public static Holder<SoundEvent> findStereo(final Holder<SoundEvent> soundEvent) {
		return toStereoEvent.computeIfAbsent(
			soundEvent.value().location(),
			location -> findStereoInternal(soundEvent, location)
		);
	}

	private static Holder<SoundEvent> findStereoInternal(
		final Holder<SoundEvent> soundEvent,
		final ResourceLocation location
	) {
		final var stereo = findStereo(location);

		if (stereo.equals(location)) {
			return soundEvent;
		}

		return new Holder.Direct<>(SoundEvent.createVariableRangeEvent(stereo));
	}

	public static ResourceLocation findStereo(final ResourceLocation location) {
		return toStereo.computeIfAbsent(location, Sounds::findStereoInternal);
	}

	private static ResourceLocation findStereoInternal(final ResourceLocation location) {
		final SoundManager soundManager = Minecraft.getInstance().getSoundManager();
		final List<String> path = new ArrayList<>(Arrays.asList(location.getPath().split("\\.")));

		final var itr = path.listIterator();
		do {
			itr.add("stereo");

			final var trial = location.withPath(String.join(".", path));

			if (soundManager.getSoundEvent(trial) != null) {
				logger.debug("Stereo found: {} => {}", location, trial);
				return trial;
			}

			logger.debug("Not present: {} => {}", location, trial);


			itr.previous();
			itr.remove();
			if (itr.hasNext()) {
				itr.next();
			} else {
				break;
			}
		} while(true);

		logger.debug("Stereo missing: {} => ???", location);

		// No stereo version found, return as-is.
		return location;
	}

	private Sounds() {
	}

	@Override
	public ResourceLocation getFabricId() {
		return id;
	}

	@Override
	public Collection<ResourceLocation> getFabricDependencies() {
		return Collections.singleton(ResourceReloadListenerKeys.SOUNDS);
	}

	@Override
	public CompletableFuture<Void> reload(final PreparationBarrier preparationBarrier, final ResourceManager resourceManager, final Executor executor, final Executor executor2) {
		// This is literally all we need to do.
		executor2.execute(() -> {
			toStereoEvent.clear();
			toStereo.clear();
		});

		return CompletableFuture.completedFuture(null);
	}
}
