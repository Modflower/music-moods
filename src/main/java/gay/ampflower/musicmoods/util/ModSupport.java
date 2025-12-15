package gay.ampflower.musicmoods.util;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ampflower
 * @since 0.7
 **/
public final class ModSupport {
	public static final boolean isModMenuPresent;

	static {
		#if FABRIC
		isModMenuPresent = Platform.isModLoaded("modmenu");
		#else
		// Neoforge and Forge always has a mod menu.
		isModMenuPresent = true;
		#endif
	}

	// The Immersive Music Mod
	public static final boolean timm = Platform.isModLoaded("timm");

	/**
	 * Set of mods known to Music Moods to conflict.
	 */
	private static final Set<String> conflictsKnown = Set.of("timm");

	/**
	 * Set of mods present that will cause conflicts.
	 */
	public static final Set<String> conflictsPresent;

	static {
		final var conflicts = new HashSet<>(conflictsKnown);
		conflicts.removeIf(Platform::isModNotLoaded);
		conflictsPresent = Set.copyOf(conflicts);
	}

	/**
	 * Set of mods known to Music Moods to conflict.
	 */
	private static final Set<String> tickersKnown = Set.of();

	/**
	 * List of mods with music managers that replaces vanilla's.
	 */
	public static final List<@Nullable String> tickersPresent;

	static {
		final var tickers = new ArrayList<String>();
		tickers.add(null);

		for (final var ticker : tickersKnown) {
			if (Platform.isModLoaded(ticker)) {
				tickers.add(ticker);
			}
		}

		tickersPresent = tickers;
	}

}
