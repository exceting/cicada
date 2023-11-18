package io.github.exceting.cicada.common.circuitbreaker.noop;

import io.github.exceting.cicada.common.circuitbreaker.api.CircuitBreakerClient;
import io.github.exceting.cicada.common.circuitbreaker.api.CircuitBreakerConfig;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * When the implementation of {@link CircuitBreakerClient} cannot be found,
 * use this class as the default implementation.
 */
public class NoopCircuitBreakerClient implements CircuitBreakerClient {

    @Override
    public void init(CircuitBreakerConfig config) {
        // do nothing.
    }

    @Override
    public <T> T execute(String name, Callable<T> callable) throws Exception {
        // do nothing.
        return callable.call();
    }

    @Override
    public <T> T execute(String name, Supplier<T> supplier) throws Exception {
        // do nothing.
        return supplier.get();
    }

    @Override
    public <T, R> R execute(String name, Function<T, R> function, T t) throws Exception {
        // do nothing.
        return function.apply(t);
    }

    @Override
    public void execute(String name, Runnable runnable) throws Exception {
        // do nothing.
        runnable.run();
    }

    @Override
    public boolean allowRequest(String name) {
        // always allow.
        return true;
    }

    @Override
    public void reset(String name) {
        // do nothing.
    }

    @Override
    public void onError(String name, Throwable t) {
        // do nothing.
    }

    @Override
    public void onSuccess(String name) {
        // do nothing.
    }
}
