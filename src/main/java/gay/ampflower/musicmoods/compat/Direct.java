package gay.ampflower.musicmoods.compat;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method or class as a <em>direct</em> mixin aware of Music Moods.
 * <p>
 * This avoids the automatic remapping to Music Moods' internals by {@link MixinClassAdjuster}s.
 *
 * @author Ampflower
 * @since 0.7
 **/
@ApiStatus.Experimental
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface Direct {
	String value() default "";
}
