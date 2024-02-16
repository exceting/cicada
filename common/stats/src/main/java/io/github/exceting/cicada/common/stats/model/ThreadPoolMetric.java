package io.github.exceting.cicada.common.stats.model;

public class ThreadPoolMetric {

    private static final String THREAD_POOL = "thread_pool";

    // TODO Parse to OpenTelemetry
    /*public static final CicadaGauge POOL_STATE_ACTIVE = CicadaGauge.build()
        .namespace(THREAD_POOL)
        .subsystem("threads")
        .name("active")
        .help("thread pool current active")
        .labelNames("name")
        .create()
        .register();

    public static final CicadaGauge POOL_STATE_TASK_WAITING = CicadaGauge.build()
        .namespace(THREAD_POOL)
        .subsystem("threads")
        .name("task_waiting")
        .help("thread pool current task waiting")
        .labelNames("name")
        .create()
        .register();*/

}
