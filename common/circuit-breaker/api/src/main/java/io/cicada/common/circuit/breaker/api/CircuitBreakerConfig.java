package io.cicada.common.circuit.breaker.api;

import lombok.Getter;
import lombok.Setter;

/**
 * Base config.
 */
@Setter
@Getter
public class CircuitBreakerConfig {

    private int volume = 100;

    private int sleep = 1000;

    private int errorRate = 50;

    private int halfOpenVolume = 10;

}
