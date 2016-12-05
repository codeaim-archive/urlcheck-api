package com.codeaim.urlcheck.api.task;


import com.codahale.metrics.*;
import com.codeaim.urlcheck.api.configuration.ApiConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.dropwizard.DropwizardMetricServices;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.SortedMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MetricReportTask
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ApiConfiguration apiConfiguration;
    private final ObjectMapper objectMapper;
    private final DropwizardMetricServices metricServices;
    private final MetricRegistry metricRegistry;

    @Autowired
    public MetricReportTask(
            final ApiConfiguration apiConfiguration,
            final ObjectMapper objectMapper,
            final DropwizardMetricServices metricServices,
            final MetricRegistry metricRegistry
    )
    {
        this.apiConfiguration = apiConfiguration;
        this.objectMapper = objectMapper;
        this.metricServices = metricServices;
        this.metricRegistry = metricRegistry;
    }

    public void run()
    {
        MDC.put("name", apiConfiguration.getName());
        MDC.put("correlationId", UUID.randomUUID().toString());

        logger.debug("MetricReporter received report request");

        try
        {
            String report = objectMapper.writeValueAsString(
                    Stream
                            .of(
                                    mapGauges(metricRegistry.getGauges()),
                                    mapCounters(metricRegistry.getCounters()),
                                    mapHistograms(metricRegistry.getHistograms()),
                                    mapMeters(metricRegistry.getMeters()),
                                    mapTimers(metricRegistry.getTimers())
                            )
                            .flatMap(x -> x)
                            .collect(Collectors.toMap(
                                    AbstractMap.SimpleEntry::getKey,
                                    AbstractMap.SimpleEntry::getValue
                            )));

            logger.info("Metrics report: {}", report);

            metricRegistry.getCounters().keySet().forEach(metricServices::reset);

        } catch (JsonProcessingException ex)
        {
            logger.error("MetricReporter exception thrown processing metrcs", ex);
        }
    }


    private Stream<AbstractMap.SimpleEntry<String, Object>> mapTimers(SortedMap<String, Timer> timers)
    {
        return timers
                .entrySet()
                .stream()
                .map(x -> new AbstractMap.SimpleEntry<>(
                        x.getKey(),
                        String.valueOf(x.getValue().getCount())
                ));
    }

    private Stream<AbstractMap.SimpleEntry<String, Object>> mapMeters(SortedMap<String, Meter> meters)
    {
        return meters
                .entrySet()
                .stream()
                .map(x -> new AbstractMap.SimpleEntry<>(
                        x.getKey(),
                        x.getValue().getCount()
                ));
    }

    private Stream<AbstractMap.SimpleEntry<String, Object>> mapHistograms(SortedMap<String, Histogram> histograms)
    {
        return histograms
                .entrySet()
                .stream()
                .map(x -> new AbstractMap.SimpleEntry<>(
                        x.getKey(),
                        x.getValue().getCount()
                ));
    }

    private Stream<AbstractMap.SimpleEntry<String, Object>> mapCounters(SortedMap<String, Counter> counters)
    {
        return counters
                .entrySet()
                .stream()
                .map(x -> new AbstractMap.SimpleEntry<>(
                        x.getKey(),
                        x.getValue().getCount()
                ));
    }

    private Stream<AbstractMap.SimpleEntry<String, Object>> mapGauges(SortedMap<String, Gauge> gauges)
    {
        return gauges
                .entrySet()
                .stream()
                .map(x -> new AbstractMap.SimpleEntry<>(
                        x.getKey(),
                        x.getValue().getValue()
                ));
    }
}
