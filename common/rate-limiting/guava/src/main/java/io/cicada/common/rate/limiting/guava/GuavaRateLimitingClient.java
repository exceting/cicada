package io.cicada.common.rate.limiting.guava;

import io.cicada.common.rate.limiting.api.RateLimitingClient;
import io.cicada.common.rate.limiting.api.RateLimitingConfig;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

public class GuavaRateLimitingClient implements RateLimitingClient {
    @Override
    public void init(RateLimitingConfig config) {

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
    public void refreshQpsThreshold(String name) {

    }
}
