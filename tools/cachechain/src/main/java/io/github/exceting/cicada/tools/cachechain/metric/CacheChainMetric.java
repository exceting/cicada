package io.github.exceting.cicada.tools.cachechain.metric;

import java.util.concurrent.atomic.AtomicLong;

public class CacheChainMetric {

    private final String metricName;

    public CacheChainMetric(String metricName) {
        this.metricName = metricName;
    }

    // 被击穿次数
    private final AtomicLong miss = new AtomicLong();

    // 命中的次数
    private final AtomicLong hit = new AtomicLong();

    // 发生回源的数据个数
    private final AtomicLong backSource = new AtomicLong();

    // 由于主动set导致的回源次数
    private final AtomicLong setBackSource = new AtomicLong();

    // refresh任务delay执行的次数
    private final AtomicLong delayTime = new AtomicLong();

    public void incMiss(int count) {
        miss.addAndGet(count);
    }

    public void incHit(int count) {
        hit.addAndGet(count);
    }

    // TODO Parse to OpenTelemetry
    public void incBackSource(int count) {
        // CacheChainMetrics.BACKSOURCE.set(backSource.addAndGet(count), metricName);
    }

    public void incSetBackSource(int count) {
        // CacheChainMetrics.REFRESH_BACKSOURCE.set(setBackSource.addAndGet(count), metricName);
    }

    public void incDelayTime() {
        // CacheChainMetrics.DELAY.set(delayTime.incrementAndGet(), metricName);
    }

    public long getMiss() {
        return miss.get();
    }

    public long getHit() {
        return hit.get();
    }

    public long getBackSource() {
        return backSource.get();
    }

    public long getSetBackSource() {
        return setBackSource.get();
    }

    public long getDelayTime() {
        return delayTime.get();
    }

    public void reset() {
        miss.set(0);
        hit.set(0);
        backSource.set(0);
        setBackSource.set(0);
    }
}