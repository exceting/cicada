package io.cicada.tools.logtrace.annos;

import org.slf4j.event.Level;

import java.lang.annotation.*;

/**
 * Methods with this annotation will have trace logs added to their internal conditional statements,
 * and optionally, try-catch statements can also be appended as needed.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
public @interface MethodTrace {

    /**
     * If true, catch and print exception.
     * If the original method has already caught and handled the exception, it will be invalid.
     */
    boolean exceptionLog() default false;

    /**
     * If true, the block of loop will be traced.
     * Since I cannot determine the size of your loop, I am unable to assess the potential risks posed by the loop.
     * You can enable it through this configuration
     */
    boolean banLoop() default false;

    /**
     * Level of trace log.
     * Default: TRACE
     */
    Level traceLevel() default Level.TRACE;

    /**
     * If true, the collection/array parameters in your method will only print their size,
     * and the specific contents will not be printed.
     */
    boolean arrayToSize() default false;
}
