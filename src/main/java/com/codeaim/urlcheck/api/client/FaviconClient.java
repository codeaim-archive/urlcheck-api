package com.codeaim.urlcheck.api.client;

import com.codeaim.urlcheck.api.configuration.ApiConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FaviconClient
{
    private final ApiConfiguration apiConfiguration;
    private final RestTemplate restTemplate;

    @Autowired
    public FaviconClient(
            ApiConfiguration apiConfiguration,
            RestTemplate restTemplate
    )
    {
        this.apiConfiguration = apiConfiguration;
        this.restTemplate = restTemplate;
    }

    public byte[] getFavicon(String url)
    {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_OCTET_STREAM.toString());

        ResponseEntity<Void> redirect = restTemplate.getForEntity(apiConfiguration.getFaviconEndpoint().replace("{url}", url), Void.class);
        ResponseEntity<byte[]> response = restTemplate.getForEntity(redirect.getHeaders().getLocation(), byte[].class);
        ResponseEntity<byte[]> response2 = restTemplate.exchange(
                apiConfiguration.getFaviconEndpoint().replace("{url}", url),
                HttpMethod.GET,
                new HttpEntity<String>(headers),
                byte[].class);

        return response.getBody();
    }
}
