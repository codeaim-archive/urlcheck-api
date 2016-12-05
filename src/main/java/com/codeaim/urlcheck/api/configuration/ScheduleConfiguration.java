package com.codeaim.urlcheck.api.configuration;


import com.codeaim.urlcheck.api.task.MetricReportTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "urlcheck.api.scheduleDisabled", havingValue = "false", matchIfMissing = true)
public class ScheduleConfiguration
{
    private MetricReportTask metricReportTask;

    @Autowired
    public ScheduleConfiguration(
            MetricReportTask metricReportTask
    )
    {
        this.metricReportTask = metricReportTask;
    }

    @Scheduled(fixedDelayString = "${urlcheck.api.metricReportDelay}")
    public void metricReportTask()
    {
        metricReportTask.run();
    }
}
