package io.cicada.common.circuit.breaker.resilience4j;

import io.cicada.common.circuit.breaker.api.CircuitBreakerClient;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

public class Resilience4jClient implements CircuitBreakerClient {

    @Override
    public <T> T execute(Callable<T> callable) throws Exception {
        return null;
    }

    @Override
    public <T> T execute(Supplier<T> supplier) throws Exception {
        return null;
    }

    @Override
    public <T, R> R execute(Function<T, R> function, T t) throws Exception {
        return null;
    }

    @Override
    public void execute(Runnable runnable) throws Exception {

    }

    @Override
    public boolean allowRequest(String name) {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void onError(String name, Throwable t) {

    }

    @Override
    public void onSuccess(String name) {

    }
}
