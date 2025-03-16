/* Copyright 2025 Ampflower
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package gay.ampflower.musicmoods.client;

import gay.ampflower.musicmoods.Config;

/**
 * @author Ampflower
 * @since 0.6.9
 **/
public interface SoundHandler {
	/** Tells the SoundEngine to stop only music */
	void moods$stopMusic();

	/** Tells the SoundEngine to stop only sounds */
	void moods$stopSounds();

	/** Tells the SoundEngine to fade out sounds. */
	void moods$fadeSounds(float ticks);

	/** Tells the SoundEngine to clear queued sounds. */
	void moods$clearQueued();

	/** Handles sound delegation */
	default boolean moods$onInterceptStop() {
		if (!Config.seamlessTransitions && Config.seamlessSoundTransitions == 0) {
			return true;
		}

		this.moods$clearQueued();

		if (Config.seamlessSoundTransitions == 0) {
			this.moods$stopSounds();
		} else {
			this.moods$fadeSounds(Config.seamlessSoundTransitions);
		}

		if (!Config.seamlessTransitions) {
			this.moods$stopMusic();
		}

		return false;
	}
}
