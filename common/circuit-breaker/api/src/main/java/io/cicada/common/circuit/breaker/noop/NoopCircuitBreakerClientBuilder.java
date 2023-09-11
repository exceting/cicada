package io.cicada.common.circuit.breaker.noop;

import io.cicada.common.circuit.breaker.api.CircuitBreakerClient;
import io.cicada.common.circuit.breaker.api.CircuitBreakerClientBuilder;

/**
 * Create {@link NoopCircuitBreakerClient}
 */
public class NoopCircuitBreakerClientBuilder implements CircuitBreakerClientBuilder {

    @Override
    public <CONFIG> CircuitBreakerClient build(CONFIG config) {
        return new NoopCircuitBreakerClient();
    }
}
