package io.github.exceting.cicada.common.circuitbreaker.sentinel;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.google.common.collect.Sets;
import io.github.exceting.cicada.common.circuitbreaker.api.CircuitBreakerClient;
import io.github.exceting.cicada.common.circuitbreaker.api.CircuitBreakerException;
import io.github.exceting.cicada.common.circuitbreaker.api.Config;
import io.github.exceting.cicada.common.logging.LogFormat;
import io.github.exceting.cicada.common.logging.LogPrefix;
import io.github.exceting.cicada.common.logging.LoggerAdapter;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

public class SentinelCircuitBreakerClient implements CircuitBreakerClient {

    private static final Logger log = new LoggerAdapter(SentinelCircuitBreakerClient.class);

    private final Lock lock = new ReentrantLock();

    private Config globalConfig = new Config();

    private Map<String, Config> customConfig = null;

    private final Set<String> loaded = Sets.newHashSet();

    /**
     * Init or refresh global config.
     *
     * @param config new config.
     */
    @Override
    public void globalConfig(Config config) {
        lock.lock();
        try {
            this.globalConfig = config;
            if (config == null) {
                throw new IllegalArgumentException("Circuit breaker client global config is null!");
            }
            reload();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Init or refresh custom config.
     *
     * @param custom new custom configs.
     */
    @Override
    public synchronized void customConfig(Map<String, Config> custom) {
        lock.lock();
        try {
            this.customConfig = custom;
            reload();
        } finally {
            lock.unlock();
        }
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
        log.warn("SentinelCircuitBreakerClient not support reset metrics.");
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
            log.warn("The method named {} is already broken! onError is meaningless!", name, e);
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
            log.warn("The method named {} is already broken! onSuccess is meaningless!", name, e);
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    private void reload() {
        if (globalConfig == null) {
            throw new IllegalStateException(LogFormat.error("The SentinelCircuitBreakerClient is not initialized!"));
        }
        lock.lock();
        try {
            loaded.clear();
        } finally {
            lock.unlock();
        }
    }

    private void checkRules(String name) {
        if (globalConfig == null) {
            throw new IllegalStateException(LogFormat.error("The SentinelCircuitBreakerClient is not initialized!"));
        }
        if (!loaded.contains(name)) {
            lock.lock();
            try {
                if (!loaded.contains(name)) { // Double check.
                    Config c;
                    if (customConfig == null || (c = customConfig.get(name)) == null) {
                        c = globalConfig;
                    }
                    DegradeRule r = new DegradeRule(name)
                            .setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType())
                            .setCount(c.getErrorRate())
                            .setTimeWindow(c.getOpenDuration() / 1000)
                            .setMinRequestAmount(c.getVolume());
                    List<DegradeRule> rs = new ArrayList<>();
                    rs.add(r);
                    DegradeRuleManager.loadRules(rs); // Load rule for current name.
                    loaded.add(name);
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
