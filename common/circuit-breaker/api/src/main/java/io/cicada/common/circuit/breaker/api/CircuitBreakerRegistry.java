package io.cicada.common.circuit.breaker.api;

import io.cicada.common.circuit.breaker.noop.NoopCircuitBreakerClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class CircuitBreakerRegistry {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerRegistry.class);

    private static final CircuitBreakerRegistry INSTANCE = new CircuitBreakerRegistry();

    private final Map<String, CircuitBreakerClient> clientMap = new ConcurrentHashMap<>();

    private final CircuitBreakerClientBuilder circuitBreakerClientBuilder;

    /**
     * Get a {@link CircuitBreakerClientBuilder} obj by spi.
     */
    private CircuitBreakerRegistry() {
        ServiceLoader<CircuitBreakerClientBuilder> circuitBreakerClientBuilders = ServiceLoader.load(CircuitBreakerClientBuilder.class);
        List<CircuitBreakerClientBuilder> builders = new ArrayList<>();
        circuitBreakerClientBuilders.forEach(builders::add);
        if (builders.size() == 0) {
            log.warn("Can't find any implementation of 'CircuitBreakerClientBuilder', 'NoopCircuitBreakerClientBuilder' will be loaded!");
            circuitBreakerClientBuilder = new NoopCircuitBreakerClientBuilder();
        } else {
            circuitBreakerClientBuilder = builders.get(0); // Default get 1st.
        }
    }

    public void createAndRegister(String name) {
        register(name, circuitBreakerClientBuilder.build());
    }

    public void register(Map<String, CircuitBreakerClient> clients) {
        if (clients != null && clients.size() > 0) {
            clients.forEach(this::register);
        }
    }

    public void register(String name, CircuitBreakerClient circuitBreakerClient) {
        if (clientMap.containsKey(name)) {
            throw new IllegalArgumentException(String.format("The circuit client named %s is already exist!", name));
        }
        clientMap.put(name, circuitBreakerClient);
    }

    public void refresh(String name, CircuitBreakerClient circuitBreakerClient) {
        clientMap.put(name, circuitBreakerClient);
    }

    public CircuitBreakerClient get(String name) {
        return clientMap.get(name);
    }
}
