package io.ginkgo.tools.logtrace.annos;

import org.slf4j.event.Level;

import java.lang.annotation.*;

/**
 * Automatically append trace logs to the decorated methods.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
public @interface LogTrace {

    /**
     * If true, catch and print exception.
     * If the original method has already caught the exception, it will be invalid.
     */
    boolean exceptionLog() default false;

    /**
     * Level of trace log.
     * Default: TRACE
     */
    Level traceLevel() default Level.TRACE;

}
