package io.cicada.common.circuit.breaker.api;

public interface CircuitBreakerClientBuilder {

    /**
     * Create a circuit breaker obj.
     *
     * @return New circuit breaker obj.
     */
    <CONFIG> CircuitBreakerClient build(CONFIG config);
}
