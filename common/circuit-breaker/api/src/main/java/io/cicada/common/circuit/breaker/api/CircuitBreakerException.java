package io.cicada.common.circuit.breaker.api;

/**
 * If the method is circuit-broken, this exception will be thrown.
 */
public class CircuitBreakerException extends Exception {
    public CircuitBreakerException(String message) {
        super(message);
    }
}
