package io.github.exceting.cicada.common.ratelimiting.guava;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import io.github.exceting.cicada.common.logging.LogPrefix;
import io.github.exceting.cicada.common.ratelimiting.api.RateLimitingClient;
import io.github.exceting.cicada.common.ratelimiting.api.RateLimitingConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class GuavaRateLimitingClient implements RateLimitingClient {

    private Map<String, RateLimiter> limiterMap;


    @Override
    public void init(RateLimitingConfig config) {
        if (config == null || config.getConfigMap() == null || config.getConfigMap().size() == 0) {
            throw new IllegalArgumentException("Can't find any rate limiter config!");
        }
        this.limiterMap = Maps.newConcurrentMap();
        config.getConfigMap().forEach((k, v) -> limiterMap.put(k, RateLimiter.create(v.getQpsThreshold())));
    }

    @Override
    public void register(String name, RateLimitingConfig.Config config) {
        limiterMap.put(name, RateLimiter.create(config.getQpsThreshold()));
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
            log.warn("{} Can't find any rate limiter of {}!", LogPrefix.CICADA_WARN, name);
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
            log.warn("{} Can't find any rate limiter of {}!", LogPrefix.CICADA_WARN, name);
        }
    }
}
