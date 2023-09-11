package io.cicada.common.circuit.breaker.resilience4j.config;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Resilience4jProperties {

    private int volumn = 100;

    private int sleep = 1000;

    private int errorRate = 50;

    private int halfOpen = 10;
}
