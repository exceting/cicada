package io.github.exceting.cicada.common.stats.threadpools;

import javax.annotation.Nonnull;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class Executor extends ThreadPoolExecutor {

    static final RejectedExecutionHandler DEFAULT_HANDLER = new AbortPolicy();

    private final AtomicInteger submittedCount = new AtomicInteger(0);


    public Executor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, DEFAULT_HANDLER);
    }

    public Executor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
                    RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        setRejectedExecutionHandler(handler);
    }

    @Override
    public void execute(@Nonnull Runnable command) {
        submittedCount.incrementAndGet();
        super.execute(command);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        submittedCount.decrementAndGet();
    }

    @Override
    public void setRejectedExecutionHandler(@Nonnull RejectedExecutionHandler handler) {
        super.setRejectedExecutionHandler((task, executor) -> {
                submittedCount.decrementAndGet();
                handler.rejectedExecution(task, executor);
        });
    }

    public int getSubmittedCount() {
        return submittedCount.get();
    }

}
