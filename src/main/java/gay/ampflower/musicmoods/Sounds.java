/*
 * Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package gay.ampflower.musicmoods;

#if FABRIC
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
#endif
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
#if MC_1_18_OR_NEWER
import net.minecraft.core.Holder;
#endif
#if MC_1_21_11_OR_NEWER
import net.minecraft.resources.Identifier;
#else
import net.minecraft.resources.ResourceLocation;
#endif
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;

#if MC_1_16_4_OR_OLDER
import org.apache.logging.log4j.Logger;
#else
import org.slf4j.Logger;
#endif

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ampflower
 * @since 0.6.12
 **/
public final class Sounds #if(FABRIC) implements SimpleSynchronousResourceReloadListener#endif {
	private static final Logger logger = Constants.getLogger();

	private static final #if (MC_1_21_11_OR_NEWER) Identifier #else ResourceLocation #endif id = Constants.id("sounds");

	// TODO: Perhaps a config for these?
	//  The server could also broadcast what it's aware of too.
	#if MC_1_21_11_OR_NEWER
	private static final Map<Identifier, Holder<SoundEvent>> toStereoEvent = new HashMap<>();
	private static final Map<Identifier, Identifier> toStereo = new HashMap<>();
	#elif MC_1_18_OR_NEWER
	private static final Map<ResourceLocation, Holder<SoundEvent>> toStereoEvent = new HashMap<>();
	private static final Map<ResourceLocation, ResourceLocation> toStereo = new HashMap<>();
	#else
	private static final Map<ResourceLocation, SoundEvent> toStereoEvent = new HashMap<>();
	private static final Map<ResourceLocation, ResourceLocation> toStereo = new HashMap<>();
	#endif

	#if FABRIC
	static {
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new Sounds());
	}
	#endif

	#if MC_1_17_OR_OLDER
	public static SoundEvent findStereo(final SoundEvent soundEvent) {
		if (soundEvent == null) {
			logger.warn("Something has gone severely wrong, and null was passed in, bailing.");
			return null;
		}

		return toStereoEvent.computeIfAbsent(
			soundEvent.location,
			location -> findStereoInternal(soundEvent, location)
		);
	}


	private static SoundEvent findStereoInternal(
		final SoundEvent soundEvent,
		final #if (MC_1_21_11_OR_NEWER) Identifier #else ResourceLocation #endif location
	) {
		final var stereo = findStereo(location);

		if (stereo.equals(location)) {
			return soundEvent;
		}

		return new SoundEvent(stereo);
	}

	#else
	public static SoundEvent findStereo(final SoundEvent soundEvent) {
		return findStereo(Holder.direct(soundEvent)).value();
	}

	public static Holder<SoundEvent> findStereo(final Holder<SoundEvent> soundEvent) {
		if (soundEvent.value() == null) {
			logger.warn("Something has gone severely wrong, and null was passed in, bailing.");
			return Holder.direct(null);
		}

		return toStereoEvent.computeIfAbsent(
			soundEvent.value().location,
			location -> findStereoInternal(soundEvent, location)
		);
	}

	private static Holder<SoundEvent> findStereoInternal(
		final Holder<SoundEvent> soundEvent,
		final #if (MC_1_21_11_OR_NEWER) Identifier #else ResourceLocation #endif location
	) {
		final var stereo = findStereo(location);

		if (stereo.equals(location)) {
			return soundEvent;
		}

		#if MC_1_19_OR_OLDER
		return new Holder.Direct<>(new SoundEvent(stereo));
		#else
		return new Holder.Direct<>(SoundEvent.createVariableRangeEvent(stereo));
		#endif
	}
	#endif

	public static #if (MC_1_21_11_OR_NEWER) Identifier #else ResourceLocation #endif findStereo(
		final #if (MC_1_21_11_OR_NEWER) Identifier #else ResourceLocation #endif location
	) {
		return toStereo.computeIfAbsent(location, id -> {
			final SoundManager soundManager = Minecraft.getInstance().getSoundManager();
			final List<String> path = new ArrayList<>(Arrays.asList(id.getPath().split("\\.")));

			final var itr = path.listIterator();
			do {
				itr.add("stereo");

				final var trial = id.withPath(String.join(".", path));

				if (soundManager.getSoundEvent(trial) != null) {
					logger.debug("Stereo found: {} => {}", id, trial);
					return trial;
				}

				logger.debug("Not present: {} => {}", id, trial);


				itr.previous();
				itr.remove();
				if (itr.hasNext()) {
					itr.next();
				} else {
					break;
				}
			} while (true);

			logger.debug("Stereo missing: {} => ???", id);

			// No stereo version found, return as-is.
			return id;
		});
	}

	private Sounds() {
	}

	#if FABRIC
	@Override
	public #if (MC_1_21_11_OR_NEWER) Identifier #else ResourceLocation #endif getFabricId() {
		return id;
	}

	@Override
	public Collection<#if (MC_1_21_11_OR_NEWER) Identifier #else ResourceLocation #endif > getFabricDependencies() {
		return Collections.singleton(ResourceReloadListenerKeys.SOUNDS);
	}

	@Override
	public void onResourceManagerReload(final ResourceManager resourceManager) {
		toStereoEvent.clear();
		toStereo.clear();
	}
	#endif
}
