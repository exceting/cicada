package io.cicada.tools.logtrace.annos;

import java.lang.annotation.*;

/**
 * Check and init log object.
 * if you used lombok, we will do nothing and use log.xxx to print log,
 * if you have already initialized the {@link org.slf4j.Logger} object, we will use yours,
 * else, we will initial the new Logger object.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface Slf4jCheck {

    /**
     * This is a master switch that controls whether to print logs. If you haven't set any value, default is open!
     * It must be an {@link java.util.concurrent.atomic.AtomicBoolean} public,static,final constant that you need to define by yourself.
     * You can dynamically control the switch by updating the value of this constant, true is open, false is close.
     * Format: your class name#your constant
     * eg: io.cicada.mock.Test#isOpen
     */
    String isOpen() default "";

}
