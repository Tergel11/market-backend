package market.commerce.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects the authenticated caller into a controller method parameter.
 *
 * @author Tergel
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthUser {

    /** When true, an anonymous request is rejected instead of getting null. */
    boolean required() default true;
}
