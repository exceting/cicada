package io.cicada.common.circuit.breaker.noop;

import io.cicada.common.circuit.breaker.api.CircuitBreakerClient;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * When the implementation of {@link CircuitBreakerClient} cannot be found,
 * use this class as the default implementation.
 */
public class NoopCircuitBreakerClient implements CircuitBreakerClient {
    @Override
    public <T> T execute(Callable<T> callable) throws Exception {
        // do nothing.
        return callable.call();
    }

    @Override
    public <T> T execute(Supplier<T> supplier) throws Exception {
        // do nothing.
        return supplier.get();
    }

    @Override
    public <T, R> R execute(Function<T, R> function, T t) throws Exception {
        // do nothing.
        return function.apply(t);
    }

    @Override
    public void execute(Runnable runnable) throws Exception {
        // do nothing.
        runnable.run();
    }

    @Override
    public boolean allowRequest(String name) {
        // always allow.
        return true;
    }

    @Override
    public void reset() {
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
