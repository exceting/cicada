package io.cicada.tools.logtrace.annos;

import java.lang.annotation.*;

/**
 * Local variables with this annotation will be automatically logged,
 * and if the local variable is obtained through a method call, the
 * method's execution time will also be logged as needed.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.LOCAL_VARIABLE})
public @interface VarTrace {

    boolean dur() default true;
}
