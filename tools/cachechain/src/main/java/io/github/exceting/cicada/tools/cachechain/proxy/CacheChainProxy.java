package io.github.exceting.cicada.tools.cachechain.proxy;

import com.google.common.collect.*;
import io.github.exceting.cicada.tools.cachechain.cache.CacheChain;
import io.github.exceting.cicada.tools.cachechain.cache.CacheChainClient;
import io.github.exceting.cicada.tools.cachechain.metric.CacheChainMetric;
import io.github.exceting.cicada.tools.cachechain.refresh.RefreshKey;
import io.github.exceting.cicada.tools.cachechain.refresh.RefreshKeyConfig;
import io.github.exceting.cicada.tools.cachechain.refresh.RefreshKeyProcessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public class CacheChainProxy implements CacheChain {

    private final CacheChainClient cacheChainClient;

    // 缓存链
    private final CacheChainProxy next;

    private final int level;

    private final CacheChainMetric cacheChainMetric;

    private RefreshKeyProcessor refreshKeyProcessor;

    public CacheChainProxy(String name,
                           int level,
                           CacheChain cacheChain,
                           CacheChainClient cacheChainClient) {
        this.level = level;
        this.cacheChainMetric = new CacheChainMetric(String.format("%s-level-%d", name, level));
        this.next = (CacheChainProxy) cacheChain;
        this.cacheChainClient = cacheChainClient;
    }

    public CacheChainProxy(String name,
                           int level,
                           CacheChain cacheChain,
                           CacheChainClient cacheChainClient,
                           RefreshKeyConfig refreshKeyConfig) {
        this(name, level, cacheChain, cacheChainClient);
        if (refreshKeyConfig != null && refreshKeyConfig.getAheadTime() > 0 && refreshKeyConfig.getKeyMaxSize() > 0) {
            this.refreshKeyProcessor = new RefreshKeyProcessor(name, refreshKeyConfig, this);
        }
    }

    @Override
    public <K, P, V> V set(@Nonnull P param,
                           @Nullable K key,
                           @Nonnull Function<P, V> backSource) {
        V v;
        if (next == null) {
            v = backSource.apply(param);
            cacheChainMetric.incSetBackSource(1);
        } else {
            v = next.set(param, key, backSource);
            cacheChainClient.set(key, v);
        }
        return v;
    }

    @Override
    public <K, P, V> Map<K, V> sets(@Nonnull Collection<P> params,
                                    @Nonnull Function<P, K> keyProvider,
                                    @Nonnull Function<Collection<P>, Map<P, V>> backSource) {
        return sets(null, params, keyProvider, backSource);
    }

    /**
     * 批量写缓存
     *
     * @param keys        缓存key集合
     * @param params      给keyProvider和backSource使用的参数
     * @param keyProvider 生成key的函数
     * @param backSource  回源函数
     * @param <K>         最终返回Map结果的标记key
     * @param <P>         回源方法参数类型
     * @param <V>         返回的结果类型
     * @return sets集合
     */
    @SuppressWarnings(value = "unchecked")
    private <K, P, V> Map<K, V> sets(@Nullable Collection<K> keys,
                                     @Nonnull Collection<P> params,
                                     @Nonnull Function<P, K> keyProvider,
                                     @Nonnull Function<Collection<P>, Map<P, V>> backSource) {
        if (next == null) {
            Map<K, V> result = getKVResult(params, keyProvider, backSource);
            cacheChainMetric.incSetBackSource(params.size());
            return result;
        }
        if (keys == null) {
            final Collection<K> keysArray = Sets.newHashSet();
            params.forEach(param -> {
                K key = keyProvider.apply(param);
                keysArray.add(key);
            });
            keys = keysArray;
        }
        Map<K, V> result = next.sets(keys, params, keyProvider, backSource);
        cacheChainClient.sets((Map<Object, Object>) result);
        return result;
    }

    @Override
    public <K, P, V> V setSingle(@Nonnull P param,
                                 @Nullable K key,
                                 @Nonnull Function<P, V> backSource) {
        V v = backSource.apply(param);
        cacheChainClient.set(key, v);
        return v;
    }

    @SuppressWarnings(value = "unchecked")
    @Override
    public <K, P, V> Map<K, V> setsSingle(@Nonnull Collection<P> params,
                                          @Nonnull Function<P, K> keyProvider,
                                          @Nonnull Function<Collection<P>, Map<P, V>> backSource) {
        Map<K, V> result = getKVResult(params, keyProvider, backSource);
        cacheChainClient.sets((Map<Object, Object>) result);
        return result;
    }

    @SuppressWarnings(value = "unchecked")
    @Override
    public <K, P, V> V get(@Nonnull P param,
                           @Nullable K key,
                           @Nonnull Function<P, V> backSource) {
        if (next == null) {
            backSourceInc(1);
            return backSource.apply(param);
        }
        V value = (V) cacheChainClient.get(key);
        if (value != null) {
            hitInc(1);
            // 缓存命中，直接返回
            return value;
        }
        missInc(1);
        value = next.get(param, key, backSource);
        if (refreshKeyProcessor != null && cacheChainClient.getExpire() > 0) {
            attachRefresh(key, param, backSource);
        }
        if (value != null) {
            // 存入下游缓存返回的结果
            cacheChainClient.set(key, value);
        }
        return value;
    }

    @Override
    public <K, P, V> Map<P, V> gets(@Nonnull Collection<P> params,
                                    @Nonnull Function<P, K> keyProvider,
                                    @Nonnull Function<Collection<P>, Map<P, V>> backSource) {
        return gets(null, params, keyProvider, backSource, HashBiMap.create());
    }

    /**
     * 批量获取缓存数据
     *
     * @param keys        缓存key集合
     * @param params      给keyProvider和backSource使用的参数
     * @param keyProvider 生成key的函数
     * @param backSource  回源函数
     * @param keyToParam  key-id双向map
     * @param <K>         最终返回Map结果的标记key
     * @param <P>         回源方法参数类型
     * @param <V>         返回的结果类型
     * @return 结果
     */
    @SuppressWarnings(value = "unchecked")
    private <K, P, V> Map<P, V> gets(@Nullable Collection<K> keys,
                                     @Nonnull Collection<P> params,
                                     @Nonnull Function<P, K> keyProvider,
                                     @Nonnull Function<Collection<P>, Map<P, V>> backSource,
                                     @Nonnull BiMap<K, P> keyToParam) {
        if (next == null) { // 说明是L0，需调用底层回源方法来完成回源
            backSourceInc(params.size());
            return backSource.apply(params);
        }
        if (keys == null) {
            final Collection<K> keysArray = Sets.newHashSet();
            params.forEach(param -> {
                K key = keyProvider.apply(param);
                keysArray.add(key);
                keyToParam.put(key, param);
            });
            keys = keysArray;
        }
        final Collection<K> finalKeys = Lists.newArrayList(keys);
        final Collection<P> finalParams = Lists.newArrayList(params);
        Map<K, V> ownResult = (Map<K, V>) cacheChainClient.gets((Collection<Object>) finalKeys);
        if (ownResult != null && !ownResult.isEmpty()) {
            hitInc(ownResult.size());
            if (ownResult.size() == params.size()) {
                return conversionKeyToParam(ownResult, keyToParam); //全员命中，直接返回
            }
            ownResult.forEach((k, v) -> {
                finalKeys.remove(k);
                finalParams.remove(keyToParam.get(k));
            });
        }

        // 记录本层miss的缓存数
        missInc(finalParams.size());

        Map<P, V> nextResult = next.gets(finalKeys, finalParams, keyProvider, backSource, keyToParam);
        if (nextResult != null && !nextResult.isEmpty()) {
            // 将下游返回的结果存入本层的缓存服务内
            Map<K, V> result = conversionParamToKey(nextResult, keyToParam);
            cacheChainClient.sets((Map<Object, Object>) result);
            if (refreshKeyProcessor != null && cacheChainClient.getExpire() > 0) {
                result.forEach((k, v) -> {
                    P param = keyToParam.get(k);
                    attachRefresh(k, param, (p) -> {
                        Map<P, V> r = backSource.apply(Lists.newArrayList(p));
                        if (r != null) {
                            return r.get(p);
                        }
                        return null;
                    });
                });
            }
        }
        if (ownResult == null || ownResult.isEmpty()) { // 全员失效，直接返回下游结果
            return nextResult;
        }
        Map<P, V> finalResult = conversionKeyToParam(ownResult, keyToParam);
        if (nextResult != null && !nextResult.isEmpty()) {
            finalResult.putAll(nextResult);
        }
        return finalResult;
    }

    @Override
    public CacheChain next() {
        return next;
    }

    @Override
    public CacheChainClient client() {
        return cacheChainClient;
    }

    @Override
    public CacheChainMetric getMetric() {
        return cacheChainMetric;
    }

    @Override
    public RefreshKeyProcessor getRefreshProcessor() {
        return refreshKeyProcessor;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void close() throws IOException {
        if (cacheChainClient != null) {
            //自close
            cacheChainClient.close();
            //传递给下游
            next.close();
        }
    }

    /**
     * 组装缓存k-v
     *
     * @param params      给keyProvider和backSource使用的参数
     * @param keyProvider 生成key的函数
     * @param backSource  回源函数
     * @param <K>         最终返回Map结果的标记key
     * @param <P>         回源方法参数类型
     * @param <V>         返回的结果类型
     * @return 结果
     */
    private <K, P, V> Map<K, V> getKVResult(@Nonnull Collection<P> params,
                                            @Nonnull Function<P, K> keyProvider,
                                            @Nonnull Function<Collection<P>, Map<P, V>> backSource) {
        Map<P, V> backResult = backSource.apply(params);
        if (backResult != null && !backResult.isEmpty()) {
            Map<K, V> result = Maps.newHashMap();
            backResult.forEach((p, v) -> {
                K k = keyProvider.apply(p);
                result.put(k, v);
            });
            return result;
        }
        return Maps.newHashMap();
    }

    private <K, P, V> Map<P, V> conversionKeyToParam(Map<K, V> ownResult, @Nonnull BiMap<K, P> keyToParam) {
        Map<P, V> result = Maps.newHashMap();
        ownResult.forEach((k, v) -> result.put(keyToParam.get(k), v));
        return result;
    }

    private <K, P, V> Map<K, V> conversionParamToKey(Map<P, V> ownResult, @Nonnull BiMap<K, P> keyToParam) {
        Map<K, V> result = Maps.newHashMap();
        ownResult.forEach((k, v) -> result.put(keyToParam.inverse().get(k), v));
        return result;
    }

    private void missInc(int count) {
        cacheChainMetric.incMiss(count);
    }

    private void hitInc(int count) {
        cacheChainMetric.incHit(count);
    }

    private void backSourceInc(int count) {
        cacheChainMetric.incBackSource(count);
    }

    @SuppressWarnings(value = "unchecked")
    private <K, P, V> void attachRefresh(K key, P param, Function<P, V> backSource) {
        refreshKeyProcessor.putKey(RefreshKey.builder()
                .backSource((Function<Object, Object>) backSource)
                .cycle(cacheChainClient.getExpire())
                .key(key)
                .param(param)
                .lastRefreshTime(System.currentTimeMillis())
                .build());
    }
}
