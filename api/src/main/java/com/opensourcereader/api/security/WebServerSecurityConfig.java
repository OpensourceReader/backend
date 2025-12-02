package com.opensourcereader.api.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/** 웹서버 보안 설정 */
@Configuration
@EnableWebSecurity
public class WebServerSecurityConfig {

  @Bean
  protected SecurityFilterChain filterChain(
      HttpSecurity http, ObjectProvider<SecurityConfigurer> securityConfigurers) throws Exception {
    http.formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable);

    securityConfigurers
        .orderedStream()
        .forEach(securityConfigurer -> securityConfigurer.configure(http));

    return http.build();
  }
}
