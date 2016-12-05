package com.codeaim.urlcheck.api.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "urlcheck.api")
public class ApiConfiguration
{
    private String name = "api-local";
    private String emailEndpoint = "https://api.mailgun.net/v3/urlcheck.io/messages";
    private String emailAuthorizationHeader;
    private String emailVerificationUrl = "http://urlcheck.io/user/{username}/verify?emailVerificationToken={email_verification_token}";
    private String faviconEndpoint = "https://icons.better-idea.org/icon?url={url}&size=16..32..48";
    private boolean scheduleDisabled;
    private int metricReportDelay;
}
