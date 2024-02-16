package io.github.exceting.cicada.common.stats.model;

public class VersionMetric {

    private static final String NAME_SPACE = "infra";

    //TODO Parse to OpenTelemetry
    /*public static final CicadaCounter VERSION = CicadaCounter.build()
        .namespace(NAME_SPACE)
        .subsystem("cicada")
        .name("version")
        .help("cicada version")
        .labelNames("name", "version")
        .create()
        .register();*/

    private static final String version = "";

    static {
        //VERSION.inc("component/utility", Version.jarVersion);
        //VERSION.inc("component/stats", version);
    }

    public static void commonCircuitBreakerAPI(String version) {
        //VERSION.inc("common/circuit-breaker/api", version);
    }

    public static void commonCircuitBreakerResilience4j(String version) {
        //VERSION.inc("common/circuit-breaker/resilience4j", version);
    }

    public static void commonCircuitBreakerSentinel(String version) {
        //VERSION.inc("common/circuit-breaker/sentinel", version);
    }

    public static void commonRateLimitingAPI(String version) {
        //VERSION.inc("common/rate-limiting/api", version);
    }

    public static void commonRateLimitingGuava(String version) {
        //VERSION.inc("common/rate-limiting/guava", version);
    }

    public static void commonRateLimitingResilience4j(String version) {
        //VERSION.inc("common/rate-limiting/resilience4j", version);
    }

    public static void commonSerializerAPI(String version) {
        //VERSION.inc("common/serializer/api", version);
    }

    public static void toolsCacheChain(String version) {
        //VERSION.inc("tools/cachechain", version);
    }

    public static void toolsLogTrace(String version) {
        //VERSION.inc("tools/logtrace", version);
    }
}
