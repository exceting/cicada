package io.cicada.common.circuit.breaker.hystrix;

import io.cicada.common.circuit.breaker.api.CircuitBreakerClient;
import io.cicada.common.circuit.breaker.api.CircuitBreakerConfig;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

public class HystrixClient implements CircuitBreakerClient {
    @Override
    public void init(CircuitBreakerConfig config) {

    }

    @Override
    public <T> T execute(String name, Callable<T> callable) throws Exception {
        return null;
    }

    @Override
    public <T> T execute(String name, Supplier<T> supplier) throws Exception {
        return null;
    }

    @Override
    public <T, R> R execute(String name, Function<T, R> function, T t) throws Exception {
        return null;
    }

    @Override
    public void execute(String name, Runnable runnable) throws Exception {

    }

    @Override
    public boolean allowRequest(String name) {
        return false;
    }

    @Override
    public void reset(String name) {

    }

    @Override
    public void onError(String name, Throwable t) {

    }

    @Override
    public void onSuccess(String name) {

    }
}
