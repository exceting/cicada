package io.cicada.common.rate.limiting.api;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

public interface RateLimitingClient {

    /**
     * Initialize a rate limiting with its config.
     */
    void init(RateLimitingConfig config);

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
    void refreshQpsThreshold(String name);

}
