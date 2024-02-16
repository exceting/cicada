package io.github.exceting.cicada.common.stats.threadpools;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicLong;

class ForkJoin extends ForkJoinPool {

    public ForkJoin(int parallelism, ForkJoinWorkerThreadFactory factory) {
        this(parallelism, factory, null, true);
    }

    public ForkJoin(int parallelism, ForkJoinWorkerThreadFactory factory, Thread.UncaughtExceptionHandler handler, boolean asyncMode) {
        super(parallelism, factory, handler, asyncMode);
    }

    @Override
    public void execute(Runnable command) {
        super.execute(command);
    }

    public static class ForkJoinThreadFactory implements ForkJoinWorkerThreadFactory {
        private final String name;
        private final AtomicLong count = new AtomicLong();

        public ForkJoinThreadFactory(String name) {
            this.name = name;
        }

        public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return new NamedForkJoinWorkerThread(String.format("%s-%d", name, count.incrementAndGet()), pool);
        }
    }

    public static class NamedForkJoinWorkerThread extends ForkJoinWorkerThread {
        protected NamedForkJoinWorkerThread(String name, ForkJoinPool pool) {
            super(pool);
            super.setName(name);
            super.setContextClassLoader(ClassLoader.getSystemClassLoader());
        }
    }

}
