package com.codeaim.urlcheck.api.aspect;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MetricAspect
{
    private final Timer getCandidates;
    private final Counter getCandidatesCount;
    private final Timer createResults;
    private final Counter createResultsCount;
    private final Timer expireResults;
    private final Counter expireResultsCount;

    @Autowired
    public MetricAspect(
            MetricRegistry metricRegistry
    )
    {
        this.getCandidates = metricRegistry.timer("get-candidates");
        this.getCandidatesCount = metricRegistry.counter("get-candidates-count");
        this.createResults = metricRegistry.timer("create-results");
        this.createResultsCount = metricRegistry.counter("create-results-count");
        this.expireResults = metricRegistry.timer("create-results");
        this.expireResultsCount = metricRegistry.counter("create-results-count");
    }

    @Around("execution(* com.codeaim.urlcheck.api.repository.ProbeRepository.getCandidates(..))")
    public Object aroundProbeRepositoryGetCandidates(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        getCandidatesCount.inc();
        return time(getCandidates, proceedingJoinPoint);
    }

    @Around("execution(* com.codeaim.urlcheck.api.repository.ProbeRepository.createResults(..))")
    public Object aroundProbeRepositoryCreateResults(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        createResultsCount.inc();
        return time(createResults, proceedingJoinPoint);
    }

    @Around("execution(* com.codeaim.urlcheck.api.repository.ProbeRepository.expireResults(..))")
    public Object aroundProbeRepositoryExpireResults(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        expireResultsCount.inc();
        return time(expireResults, proceedingJoinPoint);
    }

    private Object time(Timer timer, ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        Timer.Context time = timer.time();

        try
        {
            return proceedingJoinPoint.proceed();
        } finally
        {
            time.stop();
        }
    }
}
