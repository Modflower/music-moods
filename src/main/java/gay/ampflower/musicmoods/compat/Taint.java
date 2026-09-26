package gay.ampflower.musicmoods.compat;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

/**
 * Prevents an adjuster from running twice, or multiple adjusters from running on the same method.
 *
 * @author Ampflower
 * @since 0.7
 **/
@Target({}) // Similar concept as MixinMerged, but modified by Music Moods instead.
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Taints.class)
public @interface Taint {
	String session = UUID.randomUUID().toString();

	String session();

	String target();

	String mixin();

	Class<? extends MixinClassAdjuster>[] adjusters();
}
