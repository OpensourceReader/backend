package com.opensourcereader.api.security;

import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.stereotype.Component;

/** CSRF 보안 설정 */
@Component
@Order(0)
public class CsrfConfigurer implements SecurityConfigurer {

  @Override
  public void configure(HttpSecurity http) {
    try {
      http.csrf(AbstractHttpConfigurer::disable);
    } catch (Exception e) {
      throw new RuntimeException("CSRF Error", e);
    }
  }
}
