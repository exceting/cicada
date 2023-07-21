package io.cicada.tools.logtrace.annos;

import java.lang.annotation.*;

/**
 * Check and init log object, if you use lombok, we will do nothing and use log.xxx to print log,
 * if you have already initialized the {@link org.slf4j.Logger} object, we will use your object,
 * else, we will initial the Logger object and use it.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface Slf4jCheck {
}
