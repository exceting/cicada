package io.cicada.common.circuit.breaker.resilience4j;

import com.google.common.base.Preconditions;
import io.cicada.common.circuit.breaker.api.CircuitBreakerConfig;
import io.cicada.common.circuit.breaker.api.CircuitBreakerClient;
import io.cicada.common.circuit.breaker.api.CircuitBreakerException;
import io.cicada.common.logging.LogPrefix;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerOpenException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

public class Resilience4jCircuitBreakerClient implements CircuitBreakerClient {

    private CircuitBreakerRegistry globalRegistry;

    private Map<String, CircuitBreakerRegistry> breakerRegistryMap;

    @Override
    public void init(CircuitBreakerConfig config) {
        Preconditions.checkNotNull(config);
        globalRegistry = CircuitBreakerRegistry.of(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .failureRateThreshold(config.getGlobal().getErrorRate())
                .ringBufferSizeInClosedState(config.getGlobal().getVolume())
                .ringBufferSizeInHalfOpenState(config.getGlobal().getHalfOpenVolume())
                .waitDurationInOpenState(Duration.ofMillis(config.getGlobal().getOpenDuration()))
                .build());
        if (config.getCustom() != null && config.getCustom().size() > 0) {
            breakerRegistryMap = new HashMap<>();
            config.getCustom().forEach((k, v) -> breakerRegistryMap.put(k,
                    CircuitBreakerRegistry.of(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                            .enableAutomaticTransitionFromOpenToHalfOpen()
                            .failureRateThreshold(v.getErrorRate())
                            .ringBufferSizeInClosedState(v.getVolume())
                            .ringBufferSizeInHalfOpenState(v.getHalfOpenVolume())
                            .waitDurationInOpenState(Duration.ofMillis(v.getOpenDuration()))
                            .build())));
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
            throw new CircuitBreakerException(String.format("%s Function named %s is broken!", LogPrefix.CICADA_ERROR, name));
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
        if (globalRegistry == null) {
            throw new IllegalStateException(LogPrefix.CICADA_ERROR + " The 'Resilience4jCircuitBreakerClient' is not initialized!");
        }
        CircuitBreakerRegistry custom;
        if (breakerRegistryMap != null && (custom = breakerRegistryMap.get(name)) != null) {
            return custom.circuitBreaker(name);
        }
        return globalRegistry.circuitBreaker(name);
    }
}
