package io.cicada.common.circuit.breaker.resilience4j;

import com.google.common.base.Preconditions;
import io.cicada.common.circuit.breaker.api.CircuitBreakerConfig;
import io.cicada.common.circuit.breaker.api.CircuitBreakerClient;
import io.cicada.common.circuit.breaker.api.CircuitBreakerException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerOpenException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

public class Resilience4jClient implements CircuitBreakerClient {

    private CircuitBreakerRegistry breakerRegistry;

    @Override
    public void init(CircuitBreakerConfig resilience4jProperties) {
        Preconditions.checkNotNull(resilience4jProperties);
        this.breakerRegistry = CircuitBreakerRegistry.of(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .failureRateThreshold(resilience4jProperties.getErrorRate())
                .ringBufferSizeInClosedState(resilience4jProperties.getVolume())
                .ringBufferSizeInHalfOpenState(resilience4jProperties.getHalfOpenVolume())
                .waitDurationInOpenState(Duration.ofMillis(resilience4jProperties.getSleep()))
                .build());
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
            throw new CircuitBreakerException(String.format("Function named %s is broken!", name));
        }
        CircuitBreaker breaker = getBreakerByName(name);
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
        if (breakerRegistry == null) {
            throw new IllegalStateException("The 'Resilience4jClient' is not initialized!");
        }
        return breakerRegistry.circuitBreaker(name);
    }
}
