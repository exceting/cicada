package io.github.exceting.cicada.common.circuitbreaker.api;

import lombok.Getter;
import lombok.Setter;

/**
 * The circuit breaker config.
 */
@Setter
@Getter
public class Config {

    /**
     * Request window size.
     * The smallest unit of statistical failure rate when circuit breaker closed.
     */
    private int volume = 100;

    /**
     * Request window size.
     * The smallest unit of statistical failure rate when circuit breaker half-open.
     */
    private int halfOpenVolume = 10;

    /**
     * Duration of circuit breaker open state.
     * After this time, the circuit breaker will enter the half-open state. In this time,
     * it will use {@link #halfOpenVolume} as the request window to attempt recovery.
     * If the error rate exceeds {@link #errorRate}' again, it will return to the open state.
     */
    private int openDuration = 1000;

    /**
     * Error rate threshold.
     * Within the request window, if the request error rate exceeds this value,
     * the circuit breaker enters the open state.
     */
    private int errorRate = 50;

}
