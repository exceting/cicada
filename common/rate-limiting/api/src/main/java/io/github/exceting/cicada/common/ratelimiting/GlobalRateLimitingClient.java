package io.github.exceting.cicada.common.ratelimiting;

import io.github.exceting.cicada.common.ratelimiting.api.RateLimitingClient;
import io.github.exceting.cicada.common.ratelimiting.api.RateLimitingConfig;
import io.github.exceting.cicada.common.ratelimiting.noop.NoopRateLimitingClient;

import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class GlobalRateLimitingClient implements RateLimitingClient {

    public static final RateLimitingClient INSTANCE = new GlobalRateLimitingClient();

    private final RateLimitingClient internalClient;

    private GlobalRateLimitingClient() {
        internalClient = ServiceLoader.load(RateLimitingClient.class)
                .findFirst().orElseGet(NoopRateLimitingClient::new);
    }

    @Override
    public void init(RateLimitingConfig config) {
        internalClient.init(config);
    }

    @Override
    public void register(String name, RateLimitingConfig.Config config) {
        internalClient.register(name, config);
    }

    @Override
    public void callPermit(String name) {
        internalClient.callPermit(name);
    }

    @Override
    public boolean callPermit(String name, long time, TimeUnit timeUnit) {
        return internalClient.callPermit(name, time, timeUnit);
    }

    @Override
    public <T> T execute(String name, Callable<T> callable) throws Exception {
        return internalClient.execute(name, callable);
    }

    @Override
    public <T> T execute(String name, Supplier<T> supplier) throws Exception {
        return internalClient.execute(name, supplier);
    }

    @Override
    public <T, R> R execute(String name, Function<T, R> function, T t) throws Exception {
        return internalClient.execute(name, function, t);
    }

    @Override
    public void execute(String name, Runnable runnable) throws Exception {
        internalClient.execute(name, runnable);
    }

    @Override
    public void refreshQpsThreshold(String name, int qpsThreshold) {
        internalClient.refreshQpsThreshold(name, qpsThreshold);
    }
}
