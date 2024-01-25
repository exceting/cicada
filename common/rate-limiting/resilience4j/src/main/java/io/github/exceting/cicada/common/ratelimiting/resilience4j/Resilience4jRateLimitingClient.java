package io.github.exceting.cicada.common.ratelimiting.resilience4j;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.exceting.cicada.common.ratelimiting.api.RateLimitingClient;
import io.github.exceting.cicada.common.ratelimiting.api.RateLimitingConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

public class Resilience4jRateLimitingClient implements RateLimitingClient {

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
            limiterMap.put(name, RateLimiter.of(name, RateLimiterConfig.custom()
                    .limitRefreshPeriod(Duration.ofSeconds(1))
                    .limitForPeriod(config.getQpsThreshold())
                    .timeoutDuration(Duration.ofSeconds(0))
                    .build()));
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

    }

    @Override
    public boolean callPermit(String name, long time, TimeUnit timeUnit) {
        //limiterMap.get(name).executeCallable()
        return false;
    }

    @Override
    public <T> T execute(String name, Callable<T> callable) throws Exception {
        return null;
    }

    @Override
    public <T> T execute(String name, Supplier<T> supplier) throws Exception {
        return null;
    }

    @Override
    public <T, R> R execute(String name, Function<T, R> function, T t) throws Exception {
        return null;
    }

    @Override
    public void execute(String name, Runnable runnable) throws Exception {

    }

    @Override
    public void refreshQpsThreshold(String name, int qpsThreshold) {

    }
}
