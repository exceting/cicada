package io.github.exceting.cicada.common.ratelimiting.resilience4j;

import io.github.exceting.cicada.common.ratelimiting.api.RateLimitingClient;
import io.github.exceting.cicada.common.ratelimiting.api.RateLimitingConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class Resilience4jRateLimitingClient implements RateLimitingClient {

    private Map<String, RateLimiter> limiterMap;

    @Override
    public void init(RateLimitingConfig config) {
        if (config == null || config.getConfigMap() == null || config.getConfigMap().size() == 0) {
            throw new IllegalArgumentException("Can't find any rate limiter config!");
        }
        this.limiterMap = new ConcurrentHashMap<>();
        config.getConfigMap().forEach((k, v) -> limiterMap.put(k, RateLimiter.of(k, RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(v.getQpsThreshold())
                .timeoutDuration(Duration.ofSeconds(0))
                .build())));
    }

    @Override
    public void register(String name, RateLimitingConfig.Config config) {
        limiterMap.put(name, RateLimiter.of(name, RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(config.getQpsThreshold())
                .timeoutDuration(Duration.ofSeconds(0))
                .build()));
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
