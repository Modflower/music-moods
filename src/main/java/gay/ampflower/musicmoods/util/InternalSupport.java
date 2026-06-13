package gay.ampflower.musicmoods.util;

import gay.ampflower.musicmoods.Constants;
import gay.ampflower.musicmoods.Sounds;
import gay.ampflower.musicmoods.client.MusicHandler;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.ApiStatus;

#if MC_1_16_4_OR_OLDER
import org.apache.logging.log4j.Logger;
#else
import org.slf4j.Logger;
#endif

import java.util.Set;

/**
 * Internals that should be configurations, but have not been properly made into a configuration yet.
 *
 * @author Ampflower
 * @since 0.7.0
 **/
@ApiStatus.Internal
public final class InternalSupport {
	private static final Logger logger = Constants.getLogger();

	// TODO: unhardcode this for mod support.
	public static final Set<SoundSource> musicalSources = Set.of(
		SoundSource.MUSIC,
		SoundSource.RECORDS
	);
	// TODO: unhardcode this for mod support.
	public static final Set<SoundSource> ignoredSources = Set.of(
		#if (MC_1_21_6_OR_NEWER) SoundSource.UI #endif
	);

	/**
	 * Handles reloading classes and instances that requires runtime data that may be invalidated by a pack reload.
	 */
	public static void reload() {
		Sounds.reload();
		MusicHandler.instance.moods$reload();
	}
}
