package io.github.exceting.cicada.common.stats.threadpools;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.github.exceting.cicada.common.stats.ScheduledStats;
import lombok.extern.slf4j.Slf4j;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.*;

/**
 * Building a thread pool object with metrics.
 * Used for create any thread pool, same as {@link Executor}.
 */
@Slf4j
public class MetricThreadPools {

    /**
     * 默认阻塞系数
     */
    private static final double DEFAULT_BLOCKING_COEFFICIENT = 0.9;

    /**
     * 默认线程池并发度
     * 核数 / (1 - 阻塞系数)
     */
    private static final int DEFAULT_PARALLELISM = (int) (Runtime.getRuntime().availableProcessors() / (1 - DEFAULT_BLOCKING_COEFFICIENT));

    public static final ForkJoinPool common;

    static {
        common = newForkJoinPool("Common-Fork-Join-Pool", DEFAULT_PARALLELISM);
        try {
            Field field = ForkJoinPool.class.getDeclaredField("common");
            field.setAccessible(true);
            boolean isFinal = Modifier.isFinal(field.getModifiers());
            Field modifiers = getModifiersField();
            if (isFinal) {
                modifiers.setAccessible(true);
                modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            }
            field.set(null, common);
            if (isFinal) {
                modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.warn("Can't find declared field: common", e);
        }
        log.info("ForkJoinPool's common pool field is replaced.");
    }

    private static Field getModifiersField() throws IllegalAccessException, NoSuchFieldException {
        // This is copied from https://github.com/powermock/powermock/pull/1010/files to work around JDK 12+
        Field modifiersField = null;
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
        } catch (NoSuchFieldException e) {
            try {
                Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
                boolean accessibleBeforeSet = getDeclaredFields0.isAccessible();
                getDeclaredFields0.setAccessible(true);
                Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
                getDeclaredFields0.setAccessible(accessibleBeforeSet);
                for (Field field : fields) {
                    if ("modifiers".equals(field.getName())) {
                        modifiersField = field;
                        break;
                    }
                }
                if (modifiersField == null) {
                    throw e;
                }
            } catch (NoSuchMethodException | InvocationTargetException ex) {
                e.addSuppressed(ex);
                throw e;
            }
        }
        return modifiersField;
    }

    public static ThreadPoolExecutor newThreadPoolExecutor(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        return newThreadPoolExecutor(name, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, true);
    }

    public static ThreadPoolExecutor newThreadPoolExecutor(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, boolean daemon) {
        ThreadPoolExecutor executor = new Executor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, getFactory(name, daemon));
        attachMonitor(name, executor);
        return executor;
    }

    public static ThreadPoolExecutor newThreadPoolExecutor(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                                           RejectedExecutionHandler handler) {
        return newThreadPoolExecutor(name, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler, true);
    }

    public static ThreadPoolExecutor newThreadPoolExecutor(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                                           RejectedExecutionHandler handler, boolean daemon) {
        ThreadPoolExecutor executor = new Executor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, getFactory(name, daemon), handler);
        attachMonitor(name, executor);
        return executor;
    }

    public static ScheduledThreadPoolExecutor newScheduledThreadPool(String name, int corePoolSize) {
        return newScheduledThreadPool(name, corePoolSize, true);
    }

    public static ScheduledThreadPoolExecutor newScheduledThreadPool(String name, int corePoolSize, boolean daemon) {
        ScheduledExecutor executor = new ScheduledExecutor(corePoolSize, getFactory(name, daemon));
        attachMonitor(name, executor);
        return executor;
    }

    public static ScheduledThreadPoolExecutor newScheduledThreadPool(String name, int corePoolSize, RejectedExecutionHandler handler) {
        return newScheduledThreadPool(name, corePoolSize, handler, true);
    }

    public static ScheduledThreadPoolExecutor newScheduledThreadPool(String name, int corePoolSize, RejectedExecutionHandler handler, boolean daemon) {
        ScheduledExecutor executor = new ScheduledExecutor(corePoolSize, getFactory(name, daemon), handler);
        attachMonitor(name, executor);
        return executor;
    }

    public static ThreadPoolExecutor newCachedThreadPool(String name) {
        return newCachedThreadPool(name, true);
    }

    public static ThreadPoolExecutor newCachedThreadPool(String name, boolean daemon) {
        ThreadPoolExecutor executor = new Executor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(), getFactory(name, daemon));
        attachMonitor(name, executor);
        return executor;
    }

    public static ThreadPoolExecutor newFixedThreadPool(String name, int nThreads) {
        return newFixedThreadPool(name, nThreads, true);
    }

    public static ThreadPoolExecutor newFixedThreadPool(String name, int nThreads, boolean daemon) {
        ThreadPoolExecutor executor = new Executor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), getFactory(name, daemon));
        attachMonitor(name, executor);
        return executor;
    }

    public static ForkJoinPool newForkJoinPool(String name, int parallelism) {
        ForkJoin forkJoin = new ForkJoin(parallelism, getForkJoinFactory(name));
        attachMonitor(name, forkJoin);
        return forkJoin;
    }

    public static ForkJoinPool newForkJoinPool(String name, int parallelism, Thread.UncaughtExceptionHandler handler, boolean asyncMode) {
        ForkJoin forkJoin = new ForkJoin(parallelism, getForkJoinFactory(name), handler, asyncMode);
        attachMonitor(name, forkJoin);
        return forkJoin;
    }

    private static ThreadFactory getFactory(String name, boolean daemon) {
        return new ThreadFactoryBuilder()
                .setNameFormat(name + "-%d")
                .setDaemon(daemon)
                .build();
    }

    private static ForkJoin.ForkJoinThreadFactory getForkJoinFactory(String name) {
        return new ForkJoin.ForkJoinThreadFactory(name);
    }

    private static void attachMonitor(String name, AbstractExecutorService executor) {
        WeakReference<AbstractExecutorService> executorReference = new WeakReference<>(executor);
        ScheduledStats.scheduleWithFixedDelay(() -> {
            AbstractExecutorService currentExecutor = executorReference.get();
            if (currentExecutor == null) {
                throw new IllegalArgumentException("Current executor reference is null, cancel the task");
            }
            if (currentExecutor.isShutdown()) {
                throw new IllegalArgumentException("Current executor reference is shutdown, cancel the task");
            }
            // TODO Parse to OpenTelemetry
            if (currentExecutor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor t = (ThreadPoolExecutor) currentExecutor;
                //ThreadPoolMetric.POOL_STATE_ACTIVE.set(t.getActiveCount(), name); // 记录正在积极执行任务的线程的大致数目
                if (!(currentExecutor instanceof ScheduledThreadPoolExecutor)) {
                    //ThreadPoolMetric.POOL_STATE_TASK_WAITING.set(t.getQueue().size(), name); // 未开始执行的任务数量
                }
            }
            if (currentExecutor instanceof ForkJoinPool) {
                ForkJoinPool f = (ForkJoinPool) currentExecutor;
                //ThreadPoolMetric.POOL_STATE_ACTIVE.set(f.getActiveThreadCount(), name); // 记录运行中或正在窃取任务的线程数
                //ThreadPoolMetric.POOL_STATE_TASK_WAITING.set(f.getQueuedSubmissionCount(), name); // 未开始执行的任务数量
            }

        }, 1, 1, TimeUnit.SECONDS);
    }
}
