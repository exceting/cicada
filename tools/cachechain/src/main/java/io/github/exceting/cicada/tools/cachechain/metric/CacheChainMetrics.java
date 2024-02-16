package io.github.exceting.cicada.tools.cachechain.metric;

class CacheChainMetrics {

    private static final String NAME_SPACE = "cache_chain";

    private static final String version = "";

    // TODO Parse to OpenTelemetry
    static {
        //VersionMetric.middlewareCacheChain(version);
    }

    /*public static final CicadaGauge DELAY = CicadaGauge.build()
        .namespace(NAME_SPACE)
        .subsystem("cachechain")
        .name("delay")
        .help("cache chain refresh delay")
        .labelNames("name")
        .create()
        .register();

    public static final CicadaGauge KEEPALIVETASK = CicadaGauge.build()
        .namespace(NAME_SPACE)
        .subsystem("cachechain")
        .name("keepalive_task")
        .help("cache chain keep alive task")
        .labelNames("name")
        .create()
        .register();

    public static final CicadaGauge KEEPALIVEKEY = CicadaGauge.build()
        .namespace(NAME_SPACE)
        .subsystem("cachechain")
        .name("keepalive_key")
        .help("cache chain keep alive key")
        .labelNames("name")
        .create()
        .register();

    public static final CicadaGauge REFRESH_BACKSOURCE = CicadaGauge.build()
        .namespace(NAME_SPACE)
        .subsystem("cachechain")
        .name("refresh_backsource")
        .help("cache chain refresh backsource")
        .labelNames("name")
        .create()
        .register();

    public static final CicadaGauge BACKSOURCE = CicadaGauge.build()
        .namespace(NAME_SPACE)
        .subsystem("cachechain")
        .name("backsource")
        .help("cache chain backsource")
        .labelNames("name")
        .create()
        .register();*/

}
