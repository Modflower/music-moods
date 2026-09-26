package gay.ampflower.musicmoods.compat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Ampflower
 * @since 0.7
 **/
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
@Deprecated(forRemoval = true)
public @interface Marker {
	String value();
}
