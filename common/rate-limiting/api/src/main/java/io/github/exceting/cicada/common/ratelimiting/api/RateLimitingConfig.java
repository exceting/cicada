package io.github.exceting.cicada.common.ratelimiting.api;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Setter
@Getter
public class RateLimitingConfig {

    /**
     * Every method's config.
     */
    private Map<String, Config> configMap = new ConcurrentHashMap<>();

    @Setter
    @Getter
    public static class Config {

        /**
         * Qps threshold.
         */
        private int qpsThreshold;
    }

}
