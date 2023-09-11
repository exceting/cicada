package io.cicada.common.circuit.breaker.api;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Interface of circuit breaker.
 */
public interface CircuitBreakerClient {

    /**
     * Execute for {@link Callable}
     *
     * @param callable Method of requiring circuit breaker listening.
     * @param <T>      Result type.
     * @return The result of callable.
     * @throws Exception Callable execution exception, or throw {@link CircuitBreakerException}.
     */
    <T> T execute(Callable<T> callable) throws Exception;

    /**
     * Execute for {@link Supplier}
     */
    <T> T execute(Supplier<T> supplier) throws Exception;

    /**
     * Execute for {@link Function}
     */
    <T, R> R execute(Function<T, R> function, T t) throws Exception;

    /**
     * Execute for {@link Runnable}
     */
    void execute(Runnable runnable) throws Exception;

    /**
     * If request is allowed? If broken, return false, else return true.
     *
     * @param name Circuit breaker name.
     * @return true: allow, false: not allow
     */
    boolean allowRequest(String name);

    /**
     * Reset the circuit breaker, and init statistics.
     */
    void reset();

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
