package com.opensourcereader.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** http 요청 설정기 */
@Component
public class AuthorizationConfigurers {

  @Bean
  @Order
  SecurityConfigurer permitAllAuthorizationConfigurer() {
    return http -> {
      try {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
      } catch (Exception e) {
        throw new RuntimeException("Http Request Error", e);
      }
    };
  }
}
