package com.codeaim.urlcheck.api.configuration;

import com.codahale.metrics.MetricRegistry;
import com.codeaim.urlcheck.api.metric.MetricReporter;
import com.codeaim.urlcheck.api.metric.ServiceMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.actuate.metrics.dropwizard.DropwizardMetricServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MetricConfiguration
{
    @Bean
    public MetricRegistry metricRegistry()
    {
        MetricRegistry metricRegistry = new MetricRegistry();
        metricRegistry.registerAll(new ServiceMetrics());
        return metricRegistry;
    }

    @Bean
    public MetricReporter metricReporter(
            ApiConfiguration apiConfiguration,
            ObjectMapper objectMapper,
            DropwizardMetricServices dropwizardMetricServices
    )
    {
        MetricReporter metrics = new MetricReporter(
                apiConfiguration,
                objectMapper,
                dropwizardMetricServices,
                metricRegistry()
        );
        metrics.start(
                apiConfiguration.getMetricReportDelay(),
                TimeUnit.MILLISECONDS);
        return metrics;
    }
}

