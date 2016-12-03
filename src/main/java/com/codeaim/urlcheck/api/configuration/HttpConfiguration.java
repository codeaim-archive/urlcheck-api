package com.codeaim.urlcheck.api.configuration;

import com.codeaim.urlcheck.api.filter.CorrelationIdFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.servlet.Filter;

@Configuration
public class HttpConfiguration
{
    @Autowired
    ApiConfiguration apiConfiguration;

    @Bean
    RestTemplate restTemplate()
    {
        return new RestTemplate();
    }

    @Bean
    Filter correlationIdFilter ()
    {
        return new CorrelationIdFilter(apiConfiguration.getName());
    }
}
