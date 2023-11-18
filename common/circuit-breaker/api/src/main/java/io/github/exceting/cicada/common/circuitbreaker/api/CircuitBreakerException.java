package io.github.exceting.cicada.common.circuitbreaker.api;

/**
 * If the method is circuit-broken, this exception will be thrown.
 */
public class CircuitBreakerException extends Exception {
    public CircuitBreakerException(String message) {
        super(message);
    }
}
