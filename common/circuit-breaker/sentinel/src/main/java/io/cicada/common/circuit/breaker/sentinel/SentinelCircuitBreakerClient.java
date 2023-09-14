package io.cicada.common.circuit.breaker.sentinel;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import io.cicada.common.circuit.breaker.api.CircuitBreakerClient;
import io.cicada.common.circuit.breaker.api.CircuitBreakerConfig;
import io.cicada.common.circuit.breaker.api.CircuitBreakerException;
import io.cicada.common.logging.LogPrefix;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class SentinelCircuitBreakerClient implements CircuitBreakerClient {

    private CircuitBreakerConfig config;

    private Map<String, Rule> allRule;

    @Override
    public void init(CircuitBreakerConfig config) {
        this.config = config;
        this.allRule = new ConcurrentHashMap<>();
    }

    @Override
    public <T> T execute(String name, Callable<T> callable) throws Exception {
        checkRules(name);
        Entry entry = null;
        try {
            entry = SphU.entry(name);
            return callable.call();
        } catch (BlockException e) {
            throw new CircuitBreakerException(e.getMessage());
        } catch (Throwable t) {
            Tracer.trace(t);
            throw t;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    @Override
    public <T> T execute(String name, Supplier<T> supplier) throws Exception {
        checkRules(name);
        Entry entry = null;
        try {
            entry = SphU.entry(name);
            return supplier.get();
        } catch (BlockException e) {
            throw new CircuitBreakerException(e.getMessage());
        } catch (Throwable t) {
            Tracer.trace(t);
            throw t;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    @Override
    public <T, R> R execute(String name, Function<T, R> function, T t) throws Exception {
        checkRules(name);
        Entry entry = null;
        try {
            entry = SphU.entry(name);
            return function.apply(t);
        } catch (BlockException e) {
            throw new CircuitBreakerException(e.getMessage());
        } catch (Throwable t2) {
            Tracer.trace(t2);
            throw t2;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    @Override
    public void execute(String name, Runnable runnable) throws Exception {
        checkRules(name);
        Entry entry = null;
        try {
            entry = SphU.entry(name);
            runnable.run();
        } catch (BlockException e) {
            throw new CircuitBreakerException(e.getMessage());
        } catch (Throwable t) {
            Tracer.trace(t);
            throw t;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    @Override
    public boolean allowRequest(String name) {
        Entry entry = null;
        try {
            entry = SphU.entry(name);
        } catch (BlockException e) {
            return false;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
        return true;
    }

    @Override
    public void reset(String name) {
        log.warn("{} Sorry 'SentinelCircuitBreakerClient' not support reset metrics.", LogPrefix.CICADA_WARN);
        // Not support.
    }

    @Override
    public void onError(String name, Throwable t) {
        Entry entry = null;
        try {
            entry = SphU.entry(name);
            if (!BlockException.isBlockException(t)) {
                Tracer.trace(t);
            }
        } catch (BlockException e) {
            log.warn("{} The method named {} is already broken! onError is meaningless!", LogPrefix.CICADA_WARN, name, e);
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    @Override
    public void onSuccess(String name) {
        Entry entry = null;
        try {
            entry = SphU.entry(name);
        } catch (BlockException e) {
            log.warn("{} The method named {} is already broken! onSuccess is meaningless!", LogPrefix.CICADA_WARN, name, e);
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    private void checkRules(String name) {
        if (config == null || allRule == null) {
            throw new IllegalStateException(LogPrefix.CICADA_ERROR + " The 'SentinelCircuitBreakerClient' is not initialized!");
        }
        if (allRule.get(name) == null) {
            synchronized (this) {
                if (allRule.get(name) == null) { // Double check.
                    CircuitBreakerConfig.Config c;
                    if (config.getCustom() == null || (c = config.getCustom().get(name)) == null) {
                        c = config.getGlobal();
                    }
                    DegradeRule r = new DegradeRule(name)
                            .setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType())
                            .setCount(c.getErrorRate())
                            .setTimeWindow(c.getOpenDuration() / 1000)
                            .setMinRequestAmount(c.getVolume());
                    List<DegradeRule> rs = new ArrayList<>();
                    rs.add(r);
                    DegradeRuleManager.loadRules(rs); // Load rule for current name.
                    allRule.put(name, r);
                }
            }
        }
    }
}
