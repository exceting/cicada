package io.github.exceting.cicada.common.ratelimiting.api;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public interface RateLimitingClient {

    /**
     * Initialize a rate limiting with its config.
     */
    void init(RateLimitingConfig config);

    /**
     * Register a new rate limiter.
     *
     * @param name   Resource name.
     * @param config Resource config.
     */
    void register(String name, RateLimitingConfig.Config config);

    /**
     * If rate-limited, this method will be blocked.
     */
    void callPermit(String name);

    /**
     * If rate-limited, this method will block according to time.
     *
     * @param name     Resource name.
     * @param time     Time of block, if set 0, the method will not be blocked.
     * @param timeUnit Time unit.
     * @return true: get permit, false: not get permit, the method is rate-limited.
     */
    boolean callPermit(String name, long time, TimeUnit timeUnit);

    /**
     * Execute for {@link Callable}
     *
     * @param name     Resource name.
     * @param callable Method of requiring rate limiting listening.
     * @param <T>      Result type.
     * @return The result of callable.
     * @throws Exception Callable execution exception, or throw {@link RateLimitingException}.
     */
    <T> T execute(String name, Callable<T> callable) throws Exception;

    /**
     * Execute for {@link Supplier}
     */
    <T> T execute(String name, Supplier<T> supplier) throws Exception;

    /**
     * Execute for {@link Function}
     */
    <T, R> R execute(String name, Function<T, R> function, T t) throws Exception;

    /**
     * Execute for {@link Runnable}
     */
    void execute(String name, Runnable runnable) throws Exception;

    /**
     * Refresh qps threshold.
     * When implementing distributed rate limiting, it may be necessary to dynamically adjust the QPS threshold.
     */
    void refreshQpsThreshold(String name, int qpsThreshold);

}
