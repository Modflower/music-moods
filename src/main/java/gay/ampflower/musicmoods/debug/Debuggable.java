package gay.ampflower.musicmoods.debug;

/**
 * @author Ampflower
 * @since 0.7.0
 */
public interface Debuggable {
	/** The total tick count of the debugged object. */
	long moods$totalTicks();

	/**
	 * The completed tick count of the debugged object.
	 *
	 * In normal scenarios, this should exactly match the {@link #moods$totalTicks() total tick count}.
	 */
	long moods$completedTicks();

	/** The last tick time of the debugged object. */
	long moods$lastTickTime();
}
