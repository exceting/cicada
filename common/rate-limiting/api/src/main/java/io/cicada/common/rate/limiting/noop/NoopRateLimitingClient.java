package io.cicada.common.rate.limiting.noop;

import io.cicada.common.rate.limiting.api.RateLimitingClient;
import io.cicada.common.rate.limiting.api.RateLimitingConfig;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class NoopRateLimitingClient implements RateLimitingClient {
    @Override
    public void init(RateLimitingConfig config) {
        // do nothing.
    }

    @Override
    public void register(String name, RateLimitingConfig.Config config) {
        // do nothing.
    }

    @Override
    public void callPermit(String name) {
        // do nothing.
    }

    @Override
    public boolean callPermit(String name, long time, TimeUnit timeUnit) {
        // do nothing.
        return true;
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
    public void refreshQpsThreshold(String name, int qpsThreshold) {
        // do nothing.
    }
}
