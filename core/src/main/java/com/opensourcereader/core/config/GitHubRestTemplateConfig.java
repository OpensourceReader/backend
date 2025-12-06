package com.opensourcereader.core.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GitHubRestTemplateConfig {

  @Value(value = "${github.personal-token}")
  private String githubAccessToken;

  @Bean
  public RestTemplate restTemplate() {
    RestTemplate rest = new RestTemplate();

    ClientHttpRequestInterceptor authInterceptor = (request, body, execution) -> {
      request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + githubAccessToken);
      return execution.execute(request, body);
    };

    rest.setInterceptors(List.of(authInterceptor));
    return rest;
  }

}
