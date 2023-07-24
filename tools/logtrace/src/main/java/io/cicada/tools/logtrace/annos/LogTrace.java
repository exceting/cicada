package io.cicada.tools.logtrace.annos;

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
     * If true, the block of loop will be traced.
     * Since I cannot determine the size of your loop, I am unable to assess the potential risks posed by the loop.
     * You can enable it through this configuration
     */
    boolean traceLoop() default false;

    /**
     * Level of trace log.
     * Default: TRACE
     */
    Level traceLevel() default Level.TRACE;

}
