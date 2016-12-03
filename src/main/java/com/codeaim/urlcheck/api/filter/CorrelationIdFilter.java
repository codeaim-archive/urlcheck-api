package com.codeaim.urlcheck.api.filter;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.*;

public class CorrelationIdFilter implements Filter
{
    private String applicationName;

    @Autowired
    public CorrelationIdFilter(
            String applicationName
    )
    {
        this.applicationName = applicationName;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(req);

        String correlationId = req.getHeader("X-Correlation-ID");
        if (correlationId == null)
            correlationId = UUID.randomUUID().toString();

        MDC.put("name", applicationName);
        MDC.put("correlationId", correlationId);
        requestWrapper.addHeader("X-Correlation-ID", correlationId);
        try
        {
            filterChain.doFilter(requestWrapper, servletResponse);
        } finally
        {
            MDC.remove("correlationId");
        }
    }

    @Override
    public void destroy()
    {
    }
}

class HeaderMapRequestWrapper extends HttpServletRequestWrapper
{
    HeaderMapRequestWrapper(HttpServletRequest request)
    {
        super(request);
    }

    private Map<String, String> headerMap = new HashMap<>();

    void addHeader(String name, String value)
    {
        headerMap.put(name, value);
    }

    @Override
    public String getHeader(String name)
    {
        String headerValue = super.getHeader(name);
        if (headerMap.containsKey(name))
        {
            headerValue = headerMap.get(name);
        }
        return headerValue;
    }

    @Override
    public Enumeration<String> getHeaderNames()
    {
        List<String> names = Collections.list(super.getHeaderNames());
        names.addAll(headerMap.keySet());
        return Collections.enumeration(names);
    }

    @Override
    public Enumeration<String> getHeaders(String name)
    {
        List<String> values = Collections.list(super.getHeaders(name));
        if (headerMap.containsKey(name))
        {
            values.add(headerMap.get(name));
        }
        return Collections.enumeration(values);
    }
}
