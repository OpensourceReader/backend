package com.opensourcereader.api.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensourcereader.api.security.jwt.JwtLoginSuccessHandler;
import com.opensourcereader.api.security.login.AuthExceptionResponseHandler;
import com.opensourcereader.api.security.login.JsonUsernamePasswordAuthenticationFilter;

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

  @Bean
  public DaoAuthenticationProvider authenticationProvider(
      UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder);

    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(
      DaoAuthenticationProvider daoAuthenticationProvider) {
    return new ProviderManager(daoAuthenticationProvider);
  }

  @Bean
  public JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordAuthenticationFilter(
      AuthenticationManager authenticationManager,
      JwtLoginSuccessHandler jwtLoginSuccessHandler,
      AuthExceptionResponseHandler loginFailureHandler,
      ObjectMapper objectMapper) {
    JsonUsernamePasswordAuthenticationFilter filter =
        new JsonUsernamePasswordAuthenticationFilter(objectMapper);
    filter.setAuthenticationManager(authenticationManager);
    filter.setAuthenticationSuccessHandler(jwtLoginSuccessHandler);
    filter.setAuthenticationFailureHandler(loginFailureHandler);
    filter.setRequiresAuthenticationRequestMatcher(
        PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/v1/auth/login"));

    return filter;
  }
}
