package com.codeaim.urlcheck.api.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Around("execution(* com.codeaim.urlcheck.api.controller.*.*(..))")
    public Object aroundControllerRequestLogging(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        logger.debug(
                "{} received {} request",
                proceedingJoinPoint.getTarget().getClass().getSimpleName(),
                proceedingJoinPoint.getSignature().getName());

        Object response = proceedingJoinPoint.proceed();

        logger.debug(
                "{} {} returned",
                proceedingJoinPoint.getTarget().getClass().getSimpleName(),
                proceedingJoinPoint.getSignature().getName());

        return response;
    }

    @Around("execution(* com.codeaim.urlcheck.api.repository.*.*(..))")
    public Object aroundRepositoryRequestLogging(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        logger.trace(
                "{} received {} request",
                proceedingJoinPoint.getTarget().getClass().getSimpleName(),
                proceedingJoinPoint.getSignature().getName());

        Object response = proceedingJoinPoint.proceed();

        logger.trace(
                "{} {} returned",
                proceedingJoinPoint.getTarget().getClass().getSimpleName(),
                proceedingJoinPoint.getSignature().getName());

        return response;
    }
}
