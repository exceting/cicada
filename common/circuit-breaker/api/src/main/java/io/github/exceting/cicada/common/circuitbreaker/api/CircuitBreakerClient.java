package io.github.exceting.cicada.common.circuitbreaker.api;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Interface of circuit breaker.
 */
public interface CircuitBreakerClient {

    /**
     * Initialize a circuit breaker with its config.
     */
    void init(CircuitBreakerConfig config);

    /**
     * Execute for {@link Callable}
     *
     * @param name     Circuit breaker name.
     * @param callable Method of requiring circuit breaker listening.
     * @param <T>      Result type.
     * @return The result of callable.
     * @throws Exception Callable execution exception, or throw {@link CircuitBreakerException}.
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
     * If request is allowed? If broken, return false, else return true.
     *
     * @param name Circuit breaker name.
     * @return true: allow, false: not allow
     */
    boolean allowRequest(String name);

    /**
     * @param name Circuit breaker name.
     *             Reset the circuit breaker, and init statistics.
     */
    void reset(String name);

    /**
     * Records a failed call.
     *
     * @param name Circuit breaker name.
     * @param t    The exception that caused this error.
     */
    void onError(String name, Throwable t);

    /**
     * Records a successful call.
     *
     * @param name Circuit breaker name.
     */
    void onSuccess(String name);

}
