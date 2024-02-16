package io.github.exceting.cicada.tools.cachechain.refresh;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
@Getter
@Builder
public class RefreshKey {
    /**
     * 回源方法入参
     */
    private final Object param;

    /**
     * key本身
     */
    private final Object key;

    /**
     * 回源方法
     */
    private final Function<Object, Object> backSource;

    /**
     * 缓存本身的有效时间
     */
    private final long cycle;

    /**
     * 上次被刷新时的时间戳
     */
    private long lastRefreshTime;

    /**
     * 这个方法在用来做延时告警
     * eg：当前key的有效期为60s，配置refresh任务每40s运行一次，但实际上任务调度很可能因为调度线程的忙碌而延时触发，
     * 当延时时间超出key的有效期时（60s）就告警，这说明调度任务已经无法保证在key过期前刷新它了
     *
     * @return delay
     */
    public long delayWarn() {
        long delay = -1;
        long now = System.currentTimeMillis();
        long realCycle = now - lastRefreshTime;
        if (realCycle > cycle) {
            delay = realCycle - cycle;
        }
        lastRefreshTime = now;
        return delay;
    }
}