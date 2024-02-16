package io.github.exceting.cicada.common.stats;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ScheduledStats {

    private static final ScheduledExecutorService scheduledStatsExecutorService;

    static {
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("ScheduledStats-%d")
                .build(),
            new ThreadPoolExecutor.DiscardPolicy());
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executor.setRemoveOnCancelPolicy(true);
        scheduledStatsExecutorService = executor;
    }

    public static void scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay, TimeUnit unit) {
        scheduledStatsExecutorService.scheduleWithFixedDelay(runnable, initialDelay, delay, unit);
    }
}
