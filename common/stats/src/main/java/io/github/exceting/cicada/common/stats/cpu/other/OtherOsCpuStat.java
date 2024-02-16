package io.github.exceting.cicada.common.stats.cpu.other;

import io.github.exceting.cicada.common.logging.LoggerAdapter;
import io.github.exceting.cicada.common.stats.ScheduledStats;
import io.github.exceting.cicada.common.stats.cpu.CpuStat;
import io.github.exceting.cicada.common.stats.cpu.OsCpuStat;
import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

public class OtherOsCpuStat implements OsCpuStat {

    private static final double NANOSECONDS_PER_SECOND = 1E9;
    private final OperatingSystemMXBean systemMXBean = ManagementFactory.getOperatingSystemMXBean();
    private volatile double cpuUseTotal;
    private volatile long cpuUsage;

    private static final Logger log = new LoggerAdapter(OtherOsCpuStat.class);


    public OtherOsCpuStat() {
        ScheduledStats.scheduleWithFixedDelay(this::refresh,
            0, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    public CpuStat geStat() {
        return CpuStat.builder()
            .usage(cpuUsage)
            .cores(Runtime.getRuntime().availableProcessors())
            .build();
    }

    private void refresh() {
        double processCpu = getProcessCpu() / NANOSECONDS_PER_SECOND;
        if (processCpu > 0) {
            if (cpuUseTotal > 0) {
                cpuUsage = new BigDecimal(String.valueOf(processCpu))
                    .subtract(new BigDecimal(String.valueOf(cpuUseTotal)))
                    .divide(new BigDecimal(String.valueOf(TimeUnit.SECONDS.toSeconds(5))), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("1000"))
                    .divide(new BigDecimal(String.valueOf(systemMXBean.getAvailableProcessors())), 4, RoundingMode.HALF_UP)
                    .longValue();
            }
            cpuUseTotal = processCpu;
        }
    }

    private long getProcessCpu() {
        try {
            Long processCpuTime = callLongGetter(systemMXBean.getClass().getMethod("GetProcessCpuTime"), systemMXBean);
            return processCpuTime == null ? -1L : processCpuTime;
        } catch (Exception e) {
            log.error("GetProcessCpuTime invoke error", e);
            return -1;
        }
    }


    private Long callLongGetter(Method method, Object obj) throws InvocationTargetException {
        try {
            return (Long) method.invoke(obj);
        } catch (IllegalAccessException e) {
            // 访问受限（可能非public）.
        }
        for (Class<?> clazz : method.getDeclaringClass().getInterfaces()) {
            try {
                Method interfaceMethod = clazz.getMethod(method.getName(), method.getParameterTypes());
                Long result = callLongGetter(interfaceMethod, obj);
                if (result != null) {
                    return result;
                }
            } catch (NoSuchMethodException e) {
                // 类可能实现了多个不相干接口.
            }
        }
        return null;
    }


}
