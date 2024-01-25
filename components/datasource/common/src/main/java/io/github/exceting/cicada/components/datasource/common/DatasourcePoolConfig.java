package io.github.exceting.cicada.components.datasource.common;

import io.github.exceting.cicada.common.circuitbreaker.api.Config;
import lombok.Getter;
import lombok.Setter;

/**
 * The common config of connection pool.
 * Follow to: org.springframework.boot.jdbc.metadata.DataSourcePoolMetadata
 */
@Setter
@Getter
public class DatasourcePoolConfig {

    /**
     * The maximum number of active connections.
     */
    private Integer max;

    /**
     * The minimum number of active connections.
     */
    private Integer min;

    /**
     * The query to use to validate that a connection is valid.
     */
    private String validationQuery;

    /**
     * The default auto-commit state of connections created by this pool. If not set
     * ({@code null}), default is JDBC driver default (If set to null then the
     * java.sql.Connection.setAutoCommit(boolean) method will not be called.)
     */
    private Boolean defaultAutoCommit;

    /**
     * The maximum number of milliseconds that a client will wait for a connection from the pool.
     */
    private Long maxWait;

    /**
     * The maximum lifetime of an idle connection in the pool.
     */
    private Long maxLifetime;

    /**
     * The global circuit breaker config of this pool.
     */
    private Config breakerGlobalConfig;

    /**
     * The custom circuit breaker config of this pool.
     */
    private Config breakerCustomConfig;
}
