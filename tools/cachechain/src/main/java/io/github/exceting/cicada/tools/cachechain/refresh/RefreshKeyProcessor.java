package io.github.exceting.cicada.tools.cachechain.refresh;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.exceting.cicada.tools.cachechain.cache.CacheChain;
import io.github.exceting.cicada.tools.cachechain.metric.RefreshKeyMetric;
import io.github.exceting.cicada.common.logging.LoggerAdapter;
import io.github.exceting.cicada.common.stats.threadpools.MetricThreadPools;
import org.slf4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RefreshKeyProcessor {

    private final ScheduledExecutorService refreshTask;
    private final CacheChain cacheChain;
    private final RefreshKeyConfig refreshKeyConfig;
    private final RefreshKeyMetric refreshKeyMetric;

    private final ReceiverThread receiverThread;

    private static final Logger log = new LoggerAdapter(RefreshKeyProcessor.class);

    public void putKey(RefreshKey key) {
        receiverThread.putKey(key);
    }

    public RefreshKeyProcessor(String name, RefreshKeyConfig refreshKeyConfig, CacheChain cacheChain) {
        this.refreshKeyConfig = refreshKeyConfig;
        this.cacheChain = cacheChain;
        this.refreshKeyMetric = new RefreshKeyMetric(name);
        refreshTask = MetricThreadPools.newScheduledThreadPool("CacheChain-RefreshKey", refreshKeyConfig.getThreadSize());
        receiverThread = new ReceiverThread("RefreshKey-Receiver", this);
        receiverThread.start();
    }

    private void attachTask(RefreshKey key) {
        // 预加载
        long ahead = key.getCycle() - (refreshKeyConfig.getAheadTime() + (long) Math.floor(Math.random() * refreshKeyConfig.getAheadTime()));
        if (ahead >= 0) {
            refreshTask.scheduleWithFixedDelay(() -> task(key), ahead, ahead, TimeUnit.MILLISECONDS);
            refreshKeyMetric.incKeepAliveTask();
        }
    }

    protected void task(RefreshKey key) {
        if (!receiverThread.existKey(key.getKey())) {
            //如果缓存里已经不存在对应的key了，则移除任务
            refreshKeyMetric.decKeepAliveTask();
            log.warn("Refresh task cancel, key named {}", key.getKey());
            throw new IllegalArgumentException("The key named " + key.getKey() + " already removed from refreshKeyCache!");
        }
        try {
            Object v;
            if (!refreshKeyConfig.isSingleRefresh()) { //配置了singleRefresh为false，则refresh本层及其以下所有缓存层的缓存
                v = this.cacheChain.set(key.getParam(), key.getKey(), key.getBackSource());
            } else { //配置了singleRefresh为true，则仅refresh本层缓存
                v = this.cacheChain.setSingle(key.getParam(), key.getKey(), key.getBackSource());
            }
            if (v == null) {
                refreshKeyMetric.decKeepAliveTask();
                receiverThread.removeKey(key.getKey());
                log.info("Refresh task cancel, key named {} back source equals null", key.getKey());
                throw new IllegalArgumentException("The key named " + key.getKey() + " back source equals null");
            }
        } finally {
            long delay = key.delayWarn();
            if (delay > 0) {
                log.warn("The key={} refresh delay! The delay time is {}ms", key.getKey(), delay);
                cacheChain.getMetric().incDelayTime();
            }
        }

    }

    public RefreshKeyMetric getMetric() {
        return refreshKeyMetric;
    }

    static class ReceiverThread extends Thread {

        private final RefreshKeyProcessor processor;
        private final BlockingQueue<RefreshKey> refreshKeyQueue = new LinkedBlockingQueue<>();
        private Cache<Object, RefreshKey> refreshKeyCache;
        private ScheduledExecutorService keepAliveTrigger;

        public ReceiverThread(String name, RefreshKeyProcessor processor) {
            super(name);
            setDaemon(true);
            this.processor = processor;
            initKeyCache();
            initKeepAlive();
        }

        private void initKeyCache() {
            refreshKeyCache = Caffeine.newBuilder()
                .initialCapacity(processor.refreshKeyConfig.getKeyMinSize())
                .maximumSize(processor.refreshKeyConfig.getKeyMaxSize())
                .expireAfterAccess(360, TimeUnit.SECONDS)
                .build();
        }

        private void initKeepAlive() {
            keepAliveTrigger = MetricThreadPools.newScheduledThreadPool("CacheChain-KeepAlive", 1);
        }

        private void putKey(RefreshKey key) {
            refreshKeyQueue.offer(key);
        }

        public void run() {
            RefreshKey takeKey;
            while (true) {
                try {
                    takeKey = refreshKeyQueue.take();
                } catch (InterruptedException e) {
                    log.error("Refresh key receiver error!", e);
                    break;
                }
                final RefreshKey key = takeKey;
                if (refreshKeyCache.getIfPresent(key.getKey()) == null) { //只有缓存中不存在的key，才会走到下方逻辑
                    refreshKeyCache.put(key.getKey(), key);
                    //防止缓存过期所做的定时任务，定期访问caffeine的key
                    long ahead = 240 + (long) Math.floor(Math.random() * 90); //周期打散，保持在240~329间
                    keepAliveTrigger.scheduleWithFixedDelay(() -> {
                        RefreshKey v = refreshKeyCache.getIfPresent(key.getKey());
                        if (v == null) {
                            //已被remove，终止任务
                            log.info("key {} removed, need not keepalive, remove the keepalive task!", key.getKey());
                            throw new IllegalArgumentException("refreshKeyCache key" + key.getKey() + " removed");
                        }
                    }, ahead, ahead, TimeUnit.SECONDS);
                    processor.attachTask(key);
                }
            }
        }

        private boolean existKey(Object key) {
            return refreshKeyCache.getIfPresent(key) != null;
        }

        private void removeKey(Object key) {
            refreshKeyCache.invalidate(key);
        }
    }

}