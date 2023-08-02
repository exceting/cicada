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
     * Log language, 0: English, 1: 简体汉语, 2: 繁體漢語, 3: 日本語.
     */
    int language() default 0;

    /**
     * This is a master switch that controls whether to print logs. If you haven't set any value, default is open!
     * It must be an {@link java.util.concurrent.atomic.AtomicBoolean} public,static,final constant that you need to define by yourself.
     * You can dynamically control the switch by updating the value of this constant, true is open, false is close.
     * Format: your class package:your class name:your constant
     * eg: io.cicada.mock:Test:isOpen
     */
    String isOpen() default "";

}
