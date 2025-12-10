package com.opensourcereader.api.security;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

  @Bean
  public OAuth2UserService<OAuth2UserRequest, OAuth2User> internalOAuth2UserService() {
    return new org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService();
  }
}
