
package com.ledgerbridge.settlement.analytics;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BigQueryAnalyticsProperties.class)
public class BigQueryConfiguration {

    @Bean
    public BigQuery bigQueryClient() {
        return BigQueryOptions.getDefaultInstance().getService();
    }
}