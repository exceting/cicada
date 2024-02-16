package io.github.exceting.cicada.tools.cachechain.supports;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.exceting.cicada.common.logging.LoggerAdapter;
import io.github.exceting.cicada.tools.cachechain.cache.CacheChainClient;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public class CaffeineChain implements CacheChainClient {

    private final Cache<Object, Object> cache;

    private final long expire;

    private static final Logger log = new LoggerAdapter(CaffeineChain.class);

    public CaffeineChain(Caffeine<Object, Object> caffeineBuilder) {
        cache = caffeineBuilder.build();
        try {
            Field expireAfterWriteNanos = caffeineBuilder.getClass().getDeclaredField("expireAfterWriteNanos");
            expireAfterWriteNanos.setAccessible(true);
            long expireNanos = (long) expireAfterWriteNanos.get(caffeineBuilder);
            if (expireNanos <= 0) {
                Field expireAfterAccessNanos = caffeineBuilder.getClass().getDeclaredField("expireAfterAccessNanos");
                expireAfterAccessNanos.setAccessible(true);
                expireNanos = (long) expireAfterAccessNanos.get(caffeineBuilder);
            }
            if (expireNanos <= 0) {
                log.warn("Your expireAfterWriteNanos and expireAfterAccessNanos are both 0, the refresh key may not work!");
            }
            this.expire = expireNanos / 1000000;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }


    @Deprecated
    public CaffeineChain(Cache<Object, Object> cache) {
        this.cache = cache;
        this.expire = 0;
    }

    @Override
    public void set(Object key, Object value) {
        if (value != null) {
            cache.put(key, value);
        }
    }

    @Override
    public void sets(Map<Object, Object> items) {
        if (items != null) {
            cache.putAll(items);
        }
    }

    @Override
    public Object get(Object key) {
        return cache.getIfPresent(key);
    }

    @Override
    public Map<Object, Object> gets(Collection<Object> keys) {
        return cache.getAllPresent(keys);
    }

    @Override
    public long getExpire() {
        return expire;
    }

    @Override
    public void close() {
        cache.cleanUp();
    }
}
