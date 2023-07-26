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
}
