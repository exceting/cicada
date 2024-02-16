package io.github.exceting.cicada.tools.cachechain.cache;

import java.io.Closeable;
import java.util.Collection;
import java.util.Map;

public interface CacheChainClient extends Closeable {

    /**
     * set缓存
     *
     * @param key   缓存key
     * @param value 缓存value
     */
    void set(Object key, Object value);

    /**
     * 批量set缓存
     *
     * @param items 缓存k-v集合
     */
    void sets(Map<Object, Object> items);

    /**
     * 获取缓存数据
     *
     * @param key 缓存key
     * @return value值
     */
    Object get(Object key);

    /**
     * 批量获取缓存数据
     *
     * @param keys 缓存key集合
     * @return 缓存k-v集合
     */
    Map<Object, Object> gets(Collection<Object> keys);

    /**
     * 本层缓存每个缓存数据的有效时间
     *
     * @return 缓存有效时间，单位：毫秒
     */
    long getExpire();
}