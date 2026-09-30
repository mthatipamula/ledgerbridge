package com.ledgerbridge.settlement.analytics;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ledgerbridge.analytics.bigquery")
public record BigQueryAnalyticsProperties(
        String projectId,
        String datasetId,
        String tableId
) {}