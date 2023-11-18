package io.github.exceting.cicada.tools.logtrace.annos;

import java.lang.annotation.*;

/**
 * Local variables with this annotation will be automatically logged.
 * It's worth noting that if the variable type is non-primitive, to ensure its information is detailed in the logs,
 * please override the {@link Object#toString()} method.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.LOCAL_VARIABLE})
public @interface VarLog {

    /**
     * If true, the duration to get this variable will be printed.
     */
    boolean dur() default false;
}
