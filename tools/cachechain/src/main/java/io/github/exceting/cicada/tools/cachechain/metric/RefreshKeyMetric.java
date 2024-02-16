package io.github.exceting.cicada.tools.cachechain.metric;

import java.util.concurrent.atomic.AtomicLong;

public class RefreshKeyMetric {

    private final String metricName;

    //keep-alive的任务数
    private final AtomicLong keepAliveTask = new AtomicLong();

    public RefreshKeyMetric(String metricName) {
        this.metricName = metricName;
    }

    // TODO Parse to OpenTelemetry
    public void incKeepAliveTask() {
        //CacheChainMetrics.KEEPALIVETASK.set(keepAliveTask.incrementAndGet(), metricName);
    }

    public void decKeepAliveTask() {
        //CacheChainMetrics.KEEPALIVETASK.set(keepAliveTask.decrementAndGet(), metricName);
    }

    public long getKeepAliveTask() {
        return keepAliveTask.get();
    }

    public void reset() {
        keepAliveTask.set(0);
    }

}
