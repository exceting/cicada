/*
package io.github.exceting.cicada.tools.cachechain.support;

import io.github.exceting.cicada.tools.cachechain.cache.CacheChainClient;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class MemcacheChain implements CacheChainClient {

    private final CicadaMemcache cicadaMemcache;

    private final int maxExpire = 3600 * 24 * 30;

    //过期时间，秒，默认0，0则会启用CicadaMemcache里的默认时间
    private int expire = 0;

    public MemcacheChain(CicadaMemcache cicadaMemcache) {
        this.cicadaMemcache = cicadaMemcache;
    }

    public MemcacheChain(CicadaMemcache cicadaMemcache, int expire) {
        this.expire = expire;
        this.cicadaMemcache = cicadaMemcache;
    }

    @Override
    public void set(Object key, Object value) {
        if (value != null) {
            if (expire > 0) {
                cicadaMemcache.set(String.valueOf(key), expire, value);
            } else {
                cicadaMemcache.set(String.valueOf(key), value);
            }
        }
    }

    @Override
    public void sets(Map<Object, Object> items) {
        if (items != null) {
            if (expire > 0) {
                items.forEach((k, v) -> cicadaMemcache.set(String.valueOf(k), expire, v));
            } else {
                items.forEach((k, v) -> cicadaMemcache.set(String.valueOf(k), v));
            }
        }
    }

    @Override
    public Object get(Object key) {
        return cicadaMemcache.get((String) key);
    }

    @Override
    public Map<Object, Object> gets(Collection<Object> keys) {
        return cicadaMemcache.gets(keys);
    }

    @Override
    public long getExpire() {
        if (expire > 0 && expire < maxExpire) {
            return expire;
        }
        if (expire >= maxExpire) {
            return maxExpire;
        }
        return 300;
    }

    @Override
    public void close() throws IOException {
        cicadaMemcache.close();
    }
}
*/
