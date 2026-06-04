package gay.ampflower.musicmoods.util;

import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

/**
 * Internals that should be configurations, but have not been properly made into a configuration yet.
 *
 * @author Ampflower
 * @since 0.7.0
 **/
@ApiStatus.Internal
public final class InternalSupport {
	// TODO: unhardcode this for mod support.
	public static final Set<SoundSource> musicalSources = Set.of(
		SoundSource.MUSIC,
		SoundSource.RECORDS
	);
	// TODO: unhardcode this for mod support.
	public static final Set<SoundSource> ignoredSources = Set.of(
		#if (MC_1_21_6_OR_NEWER) SoundSource.UI #endif
	);
}
