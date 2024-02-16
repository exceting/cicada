package io.github.exceting.cicada.tools.cachechain.cache;

import io.github.exceting.cicada.tools.cachechain.metric.CacheChainMetric;
import io.github.exceting.cicada.tools.cachechain.refresh.RefreshKeyProcessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public interface CacheChain extends Closeable {

    /**
     * 写缓存链
     *
     * @param param      回源方法参数
     * @param key        缓存key
     * @param backSource 回源方法
     * @param <K>        最终返回Map结果的标记key
     * @param <P>        回源方法参数类型
     * @param <V>        返回的结果类型
     * @return set成功的value
     */
    <K, P, V> V set(@Nonnull P param, @Nullable K key,
                    @Nonnull Function<P, V> backSource);

    /**
     * 批量写缓存链
     *
     * @param params     给keyProvider和backSource使用的参数
     * @param backSource 回源方法
     * @param <K>        最终返回Map结果的标记key
     * @param <P>        回源方法参数类型
     * @param <V>        返回的结果类型
     * @return sets成功的map集合
     */
    <K, P, V> Map<K, V> sets(@Nonnull Collection<P> params, @Nonnull Function<P, K> keyProvider,
                             @Nonnull Function<Collection<P>, Map<P, V>> backSource);

    /**
     * 只写本层缓存
     *
     * @param param      回源方法参数
     * @param key        缓存key
     * @param backSource 回源方法
     * @param <K>        最终返回Map结果的标记key
     * @param <P>        回源方法参数类型
     * @param <V>        返回的结果类型
     */
    <K, P, V> V setSingle(@Nonnull P param, @Nullable K key,
                          @Nonnull Function<P, V> backSource);

    /**
     * 批量写本层缓存
     *
     * @param params     给keyProvider和backSource使用的参数
     * @param backSource 回源方法
     * @param <K>        最终返回Map结果的标记key
     * @param <P>        回源方法参数类型
     * @param <V>        返回的结果类型
     */
    <K, P, V> Map<K, V> setsSingle(@Nonnull Collection<P> params, @Nonnull Function<P, K> keyProvider,
                                   @Nonnull Function<Collection<P>, Map<P, V>> backSource);

    /**
     * 获取单个缓存数据
     *
     * @param param      回源方法参数
     * @param key        缓存key
     * @param backSource 回源方法
     * @param <K>        最终返回Map结果的标记key
     * @param <P>        回源方法参数类型
     * @param <V>        返回的结果类型
     * @return value
     */
    <K, P, V> V get(@Nonnull P param, @Nullable K key,
                    @Nonnull Function<P, V> backSource);

    /**
     * 批量获取缓存数据
     *
     * @param params      给keyProvider和backSource使用的参数
     * @param keyProvider 生成key的函数
     * @param backSource  回源函数
     * @param <K>         最终返回Map结果的标记key
     * @param <V>         返回的结果类型
     * @return values
     */
    <K, P, V> Map<P, V> gets(@Nonnull Collection<P> params, @Nonnull Function<P, K> keyProvider,
                             @Nonnull Function<Collection<P>, Map<P, V>> backSource);

    /**
     * @return 当前缓存层后续的链
     */
    CacheChain next();

    /**
     * @return 当前链内保存的CacheChainClient对象
     */
    CacheChainClient client();

    /**
     * @return 当前缓存层的监控对象
     */
    CacheChainMetric getMetric();

    /**
     * @return 当前缓存层执行Refresh任务的执行器
     */
    RefreshKeyProcessor getRefreshProcessor();

    /**
     * @return 当前缓存层处于的层级
     */
    int getLevel();

}
