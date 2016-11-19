package com.codeaim.urlcheck.api.client;

import com.codeaim.urlcheck.api.configuration.ApiConfiguration;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Configuration
public class EmailClient
{
    private final ApiConfiguration apiConfiguration;
    private final RestTemplate restTemplate;

    @Autowired
    public EmailClient(
            ApiConfiguration apiConfiguration,
            RestTemplate restTemplate
    )
    {
        this.apiConfiguration = apiConfiguration;
        this.restTemplate = restTemplate;
    }

    public void sendVerifyEmail(
            String email,
            String username,
            String emailVerificationToken
    )
    {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("from", "urlcheck.io <admin@urlcheck.io>");
        parameters.add("to", email);
        parameters.add("subject", "Please verify your urlcheck.io account");
        parameters.add("html", getVerifyEmailHtml(username, emailVerificationToken));

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.AUTHORIZATION, apiConfiguration.getEmailAuthorizationHeader());
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED.toString());

        restTemplate.postForObject(
                apiConfiguration.getEmailEndpoint(),
                new HttpEntity<>(parameters, headers),
                String.class
        );
    }

    private String getVerifyEmailHtml(String username, String emailVerificationToken)
    {
        try
        {
            return IOUtils.toString(ResourceUtils.getURL("classpath:templates/verifyEmail.html").openStream(), "UTF-8")
                    .replace("{email_verification_url}", apiConfiguration.getEmailVerificationUrl()
                            .replace("{username}", username)
                            .replace("{email_verification_token}", emailVerificationToken));
        } catch (IOException e)
        {
            throw new RuntimeException("Failed to read verify email template");
        }
    }


}
