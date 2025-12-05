package com.opensourcereader.api.security.configurer;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import com.opensourcereader.api.security.DefaultUserDetailService;
import com.opensourcereader.api.security.SecurityConfigurer;
import com.opensourcereader.api.security.jwt.AuthTokenFilter;
import com.opensourcereader.core.security.service.JwtService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FilterConfigurer {

  private final JwtService jwtService;
  private final DefaultUserDetailService userDetailsService;

  @Bean
  @Order
  SecurityConfigurer authTokenFilter() {
    return http -> {
      try {
        http.addFilterBefore(
            new AuthTokenFilter(jwtService, userDetailsService),
            UsernamePasswordAuthenticationFilter.class);
      } catch (Exception e) {
        throw new RuntimeException("Http Request Error", e);
      }
    };
  }
}
