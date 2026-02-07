package com.opensourcereader.api.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpConfig {

  @Bean
  public RestClient restClient(RestClient.Builder builder) {
    return builder
        .baseUrl("https://api.github.com")
        .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
        .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
        .build();
  }
}
