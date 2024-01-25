package io.github.exceting.cicada.common.circuitbreaker;

import io.github.exceting.cicada.common.circuitbreaker.api.CircuitBreakerClient;
import io.github.exceting.cicada.common.circuitbreaker.api.Config;
import io.github.exceting.cicada.common.circuitbreaker.noop.NoopCircuitBreakerClient;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

public class GlobalCircuitBreakerClient implements CircuitBreakerClient {

    public static final CircuitBreakerClient INSTANCE = new GlobalCircuitBreakerClient();

    private final CircuitBreakerClient internalClient;


    private GlobalCircuitBreakerClient() {
        internalClient = ServiceLoader.load(CircuitBreakerClient.class)
                .findFirst().orElseGet(NoopCircuitBreakerClient::new);
    }

    @Override
    public void globalConfig(Config config) {
        internalClient.globalConfig(config);
    }

    @Override
    public void customConfig(Map<String, Config> custom) {
        internalClient.customConfig(custom);
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
    public boolean allowRequest(String name) {
        return internalClient.allowRequest(name);
    }

    @Override
    public void reset(String name) {
        internalClient.reset(name);
    }

    @Override
    public void onError(String name, Throwable t) {
        internalClient.onError(name, t);
    }

    @Override
    public void onSuccess(String name) {
        internalClient.onSuccess(name);
    }
}
