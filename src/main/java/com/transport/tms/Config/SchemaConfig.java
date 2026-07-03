package com.transport.tms.Config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Central schema name holder.
 *
 * Defined in application.properties:
 *   db.schema  = tms     → Postgres TMS schema
 *   x3.schema  = LEWISB  → SQL Server X3 schema (tbs database)
 *
 * To change either schema, update application.properties only —
 * no code changes needed.
 */
@Component
@Getter
public class SchemaConfig {

    @Value("${db.schema}")
    private String postgresSchema;   // "tms"

    @Value("${x3.schema}")
    private String x3Schema;         // "LEWISB"
}
