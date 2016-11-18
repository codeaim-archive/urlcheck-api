package com.codeaim.urlcheck.api.client;

import com.codeaim.urlcheck.api.configuration.ApiConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EmailClient
{

    private final Resource validateAccountTemplate;

    private final ApiConfiguration apiConfiguration;
    private final RestTemplate restTemplate;


    @Autowired
    public EmailClient(
            ApiConfiguration apiConfiguration,
            RestTemplate restTemplate,
            @Value("file:")
            Resource validateAccountTemplate
    )
    {
        this.validateAccountTemplate = validateAccountTemplate;
        this.apiConfiguration = apiConfiguration;
        this.restTemplate = restTemplate;
    }

    public void validateAccountEmail(String email, String emailVerificationToken)
    {
        restTemplate.postForObject(
                apiConfiguration.getEmailEndpoint(),
                Collections.unmodifiableMap(Stream.of(
                        new SimpleEntry<>("from", "urlcheck.io <admin@urlcheck.io>"),
                        new SimpleEntry<>("to", email),
                        new SimpleEntry<>("subject", "Please verify your urlcheck.io account"),
                        new SimpleEntry<>("html", getValidateAccountHtml(emailVerificationToken)))
                        .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)))
                , String.class);
    }

    private String getValidateAccountHtml(String emailVerificationToken)
    {
        try
        {
            return new String(Files.readAllBytes(validateAccountTemplate.getFile().toPath()))
                    .replace("{email_verification_token}", emailVerificationToken);
        } catch (IOException e)
        {
            throw new RuntimeException("Failed to read validate account template");
        }
    }


}
