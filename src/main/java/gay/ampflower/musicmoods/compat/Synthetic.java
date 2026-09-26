package gay.ampflower.musicmoods.compat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a given member as a synthetic member without stacktrace omission.
 *
 * @author Ampflower
 * @since 0.7
 **/
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Synthetic {
}
