package gay.ampflower.musicmoods.debug;

import gay.ampflower.musicmoods.Constants;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

#if MC_1_16_4_OR_OLDER
import org.apache.logging.log4j.Logger;
#else
import org.slf4j.Logger;
#endif

import java.util.function.Supplier;

/**
 * @author Ampflower
 * @since 0.7.0
 **/
public final class Debugger {
	private static final Logger logger = Constants.getLogger();

	public static final Debugger musicManager = new Debugger(() -> {
		if (Minecraft.getInstance().getMusicManager() instanceof Debuggable debuggable) {
			return debuggable;
		}
		return null;
	});

	private final @NotNull Supplier<@Nullable Debuggable> debuggableSupplier;

	private long ticks = 0L;
	private long lastTickTime = 0L;

	private long tickDrift = 0L;
	private long completedTickDrift = 0L;

	private boolean tickDriftWarned = false;
	private boolean completedTickDriftWarned = false;

	private Debugger(final @NotNull Supplier<@Nullable Debuggable> debuggableSupplier) {
		this.debuggableSupplier = debuggableSupplier;
	}

	public void trackTick() {
		this.ticks++;
		this.lastTickTime = System.currentTimeMillis();
	}

	public void test() {
		final Debuggable debuggable = this.debuggableSupplier.get();

		if (debuggable == null) {
			// We can't do anything.
			return;
		}

		{
			final long timeDelta = debuggable.moods$lastTickTime() - this.lastTickTime;
			if (timeDelta > 50) {
				logger.warn("Music Moods: {} is taking an unreasonable amount of time to execute.", debuggable);
			}
		}

		{
			final long completedTickDrift = debuggable.moods$totalTicks() - debuggable.moods$completedTicks();
			if (completedTickDrift != this.completedTickDrift) {
				if (!this.completedTickDriftWarned) {
					this.completedTickDriftWarned = true;
					logger.warn("Music Moods: {} is failing to complete its task. Current drift: {}", debuggable, completedTickDrift);
					// TODO: add a class tracer here for potential mixins.
				}
				this.completedTickDrift = completedTickDrift;
			} else {
				if (completedTickDriftWarned) {
					logger.warn("Music Moods: {} failed to complete ticks for {} ticks.", debuggable, tickDrift);
				}
				this.completedTickDriftWarned = false;
			}
		}

		{
			final long tickDrift = this.ticks - debuggable.moods$totalTicks();
			if (tickDrift != this.tickDrift) {
				if (!this.tickDriftWarned) {
					this.tickDriftWarned = true;
					logger.warn("Music Moods: {} is failing to be ticked. Current drift: {}", debuggable, tickDrift);
					// TODO: add a class tracer here for potential mixins.
				}
				this.tickDrift = tickDrift;
			} else {
				if (this.tickDriftWarned) {
					logger.warn("Music Moods: {} failed to tick for {} ticks.", debuggable, tickDrift);
				}
				this.tickDriftWarned = false;
			}
		}
	}
}
