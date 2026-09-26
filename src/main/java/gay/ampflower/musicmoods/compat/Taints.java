package gay.ampflower.musicmoods.compat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Ampflower
 * @since 0.7
 **/
@Target({}) // Similar concept as MixinMerged, but modified by Music Moods instead.
@Retention(RetentionPolicy.RUNTIME)
public @interface Taints {
	Taint[] value();
}
