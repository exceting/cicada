package io.github.exceting.cicada.common.circuitbreaker.resilience4j;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import io.github.exceting.cicada.common.circuitbreaker.api.CircuitBreakerClient;
import io.github.exceting.cicada.common.circuitbreaker.api.CircuitBreakerException;
import io.github.exceting.cicada.common.circuitbreaker.api.Config;
import io.github.exceting.cicada.common.logging.LogFormat;
import io.github.exceting.cicada.common.logging.LogPrefix;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerOpenException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

public class Resilience4jCircuitBreakerClient implements CircuitBreakerClient {

    private CircuitBreakerRegistry circuitBreakerRegistry;

    private final Lock lock = new ReentrantLock();

    private Config globalConfig = new Config();

    private Map<String, CircuitBreakerConfig> customConfig = null;

    public Resilience4jCircuitBreakerClient() {
        refreshRegistry(); // Init registry.
    }

    /**
     * Init or refresh global config.
     *
     * @param config new config.
     */
    @Override
    public void globalConfig(Config config) {
        lock.lock();
        try {
            this.globalConfig = config;
            if (config == null) {
                throw new IllegalArgumentException("Circuit breaker client global config is null!");
            }
            refreshRegistry();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Init or refresh custom config.
     *
     * @param custom new custom configs.
     */
    @Override
    public synchronized void customConfig(Map<String, Config> custom) {
        lock.lock();
        try {
            if (custom != null && !custom.isEmpty()) {
                Map<String, CircuitBreakerConfig> newConfig = Maps.newHashMap();
                custom.forEach((k, v) -> newConfig.put(k, CircuitBreakerConfig.custom()
                        .enableAutomaticTransitionFromOpenToHalfOpen()
                        .failureRateThreshold(v.getErrorRate())
                        .ringBufferSizeInClosedState(v.getVolume())
                        .ringBufferSizeInHalfOpenState(v.getHalfOpenVolume())
                        .waitDurationInOpenState(Duration.ofMillis(v.getOpenDuration()))
                        .build()));
                this.customConfig = newConfig;
            } else {
                this.customConfig = null;
            }
            refreshRegistry();
        } finally {
            lock.unlock();
        }
    }


    @Override
    public <T> T execute(String name, Callable<T> callable) throws Exception {
        Preconditions.checkNotNull(name);
        CircuitBreaker breaker = getBreakerByName(name);
        try {
            return breaker.executeCallable(callable);
        } catch (CircuitBreakerOpenException e) {
            throw new CircuitBreakerException(e.getMessage());
        }
    }

    @Override
    public <T> T execute(String name, Supplier<T> supplier) throws Exception {
        Preconditions.checkNotNull(name);
        CircuitBreaker breaker = getBreakerByName(name);
        try {
            return breaker.executeSupplier(supplier);
        } catch (CircuitBreakerOpenException e) {
            throw new CircuitBreakerException(e.getMessage());
        }
    }

    @Override
    public <T, R> R execute(String name, Function<T, R> function, T t) throws Exception {
        Preconditions.checkNotNull(name);
        if (!allowRequest(name)) {
            throw new CircuitBreakerException(LogFormat.error("Function named %s is broken!", name));
        }
        try {
            R r = function.apply(t);
            onSuccess(name);
            return r;
        } catch (Exception e) {
            onError(name, e);
            throw e;
        }
    }

    @Override
    public void execute(String name, Runnable runnable) throws Exception {
        Preconditions.checkNotNull(name);
        CircuitBreaker breaker = getBreakerByName(name);
        try {
            breaker.executeRunnable(runnable);
        } catch (CircuitBreakerOpenException e) {
            throw new CircuitBreakerException(e.getMessage());
        }
    }

    @Override
    public boolean allowRequest(String name) {
        Preconditions.checkNotNull(name);
        CircuitBreaker breaker = getBreakerByName(name);
        return breaker.isCallPermitted();
    }

    @Override
    public void reset(String name) {
        Preconditions.checkNotNull(name);
        CircuitBreaker breaker = getBreakerByName(name);
        breaker.reset();
    }

    @Override
    public void onError(String name, Throwable t) {
        Preconditions.checkNotNull(name);
        CircuitBreaker breaker = getBreakerByName(name);
        breaker.onError(0, t);
    }

    @Override
    public void onSuccess(String name) {
        Preconditions.checkNotNull(name);
        CircuitBreaker breaker = getBreakerByName(name);
        breaker.onSuccess(0);
    }

    private CircuitBreaker getBreakerByName(String name) {
        if (circuitBreakerRegistry == null) {
            throw new IllegalStateException(LogFormat.error("The 'Resilience4jCircuitBreakerClient' is not initialized!"));
        }
        CircuitBreakerConfig custom;
        if (customConfig != null && (custom = customConfig.get(name)) != null) {
            return circuitBreakerRegistry.circuitBreaker(name, custom);
        }
        return circuitBreakerRegistry.circuitBreaker(name);
    }

    private void refreshRegistry() {
        circuitBreakerRegistry = CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .failureRateThreshold(globalConfig.getErrorRate())
                .ringBufferSizeInClosedState(globalConfig.getVolume())
                .ringBufferSizeInHalfOpenState(globalConfig.getHalfOpenVolume())
                .waitDurationInOpenState(Duration.ofMillis(globalConfig.getOpenDuration()))
                .build());
    }
}
