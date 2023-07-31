package io.cicada.tools.logtrace.annos;

import java.lang.annotation.*;

/**
 * The parameter annotated with {@link Ban} will not be printed in the log.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PARAMETER})
public @interface Ban {
}
