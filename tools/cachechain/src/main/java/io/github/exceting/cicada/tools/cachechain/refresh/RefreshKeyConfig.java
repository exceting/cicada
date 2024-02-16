package io.github.exceting.cicada.tools.cachechain.refresh;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshKeyConfig {

    /**
     * 这个值代表需要refresh的key的最小数量大概有多少
     * 它的值只需要保证比keyMaxSize小即可
     */
    private final int keyMinSize;

    /**
     * 这个值代表需要refresh的key的最大数量大概有多少
     * eg：最多参与refresh的key会保持在{@param keyMaxSize}个左右；
     * 淘汰制度：访问频率高的key会将访问频率低的key从refresh任务里挤出去（算法：1W-TinyLFU）
     */
    private final int keyMaxSize;

    /**
     * 提前refresh的时间，单位ms，为近似值。
     * 执行周期计算公式：本层缓存有效期-(aheadTime + random[0~aheadTime))
     * eg：当前层的缓存有效期为10000ms，所配的ahead是2000ms，这里本意是要refresh按照8000ms的周期执行，
     * 但实际的执行周期是：10000 - (2000+(0~1999间的随机数))，这是为了打散定时任务，防止大量定时任务在同一时间内被调度到导致工作线程阻塞
     */
    private final int aheadTime;

    /**
     * 负责调度本层缓存refresh key任务的线程数量
     */
    private final int threadSize;

    /**
     * 是否只refresh单层缓存，默认false
     * false：当refresh发生时，会把位于本层以下的所有缓存全部refresh一遍
     * true：当refresh发生时，只会刷新本层缓存
     */
    private final boolean singleRefresh;

}
