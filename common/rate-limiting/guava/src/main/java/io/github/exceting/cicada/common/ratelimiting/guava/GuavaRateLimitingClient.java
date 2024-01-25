package io.github.exceting.cicada.common.ratelimiting.guava;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.RateLimiter;
import io.github.exceting.cicada.common.logging.LoggerAdapter;
import io.github.exceting.cicada.common.ratelimiting.api.RateLimitingClient;
import io.github.exceting.cicada.common.ratelimiting.api.RateLimitingConfig;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

public class GuavaRateLimitingClient implements RateLimitingClient {

    private static final Logger log = new LoggerAdapter(GuavaRateLimitingClient.class);

    private final Map<String, RateLimiter> limiterMap = Maps.newHashMap();

    private final Lock lock = new ReentrantLock();

    @Override
    public void rateLimitingConfig(Map<String, RateLimitingConfig.Config> configMap) {
        lock.lock();
        try {
            Set<String> needClean;
            if (configMap == null || configMap.isEmpty()) {
                needClean = limiterMap.keySet();
            } else {
                needClean = Sets.newHashSet();
                this.limiterMap.keySet().forEach(k -> {
                    if (!configMap.containsKey(k)) {
                        needClean.add(k);
                    }
                });
                configMap.forEach(this::register);
            }
            needClean.forEach(this::unregister);

        } finally {
            lock.unlock();
        }
    }

    @Override
    public void register(String name, RateLimitingConfig.Config config) {
        lock.lock();
        try {
            limiterMap.put(name, RateLimiter.create(config.getQpsThreshold()));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void unregister(String name) {
        lock.lock();
        try {
            limiterMap.remove(name);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void callPermit(String name) {
        blockWhenLimited(name);
    }

    @Override
    public boolean callPermit(String name, long time, TimeUnit timeUnit) {
        RateLimiter rateLimiter;
        if ((rateLimiter = limiterMap.get(name)) != null) {
            return time <= 0 ? rateLimiter.tryAcquire() : rateLimiter.tryAcquire(time, timeUnit);
        } else {
            log.warn("Can't find any rate limiter of {}!", name);
        }
        return false;
    }

    @Override
    public <T> T execute(String name, Callable<T> callable) throws Exception {
        blockWhenLimited(name);
        return callable.call();
    }

    @Override
    public <T> T execute(String name, Supplier<T> supplier) throws Exception {
        blockWhenLimited(name);
        return supplier.get();
    }

    @Override
    public <T, R> R execute(String name, Function<T, R> function, T t) throws Exception {
        blockWhenLimited(name);
        return function.apply(t);
    }

    @Override
    public void execute(String name, Runnable runnable) throws Exception {
        blockWhenLimited(name);
        runnable.run();
    }

    @Override
    public void refreshQpsThreshold(String name, int qpsThreshold) {
        RateLimiter rateLimiter;
        if ((rateLimiter = limiterMap.get(name)) != null) {
            rateLimiter.setRate(qpsThreshold);
        }
    }

    private void blockWhenLimited(String name) {
        RateLimiter rateLimiter;
        if ((rateLimiter = limiterMap.get(name)) != null) {
            rateLimiter.acquire();
        } else {
            log.warn("Can't find any rate limiter of {}!", name);
        }
    }
}
