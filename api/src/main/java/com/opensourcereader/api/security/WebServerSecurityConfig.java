package com.opensourcereader.api.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

  @Bean
  protected PasswordEncoder passwordEncoder() {
    String encodingId = "argon2";
    Map<String, PasswordEncoder> encoders = new HashMap<>();
    // saltLength(16), hashLength(32), parallelism(1), memory(16384), iterations(2)
    encoders.put(encodingId, Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
    return new DelegatingPasswordEncoder(encodingId, encoders);
  }
}
