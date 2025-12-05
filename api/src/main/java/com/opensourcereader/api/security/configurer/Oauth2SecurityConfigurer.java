package com.opensourcereader.api.security.configurer;

import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

import com.opensourcereader.api.security.SecurityConfigurer;

@Component
@Order(100)
public class Oauth2SecurityConfigurer implements SecurityConfigurer {

  @Override
  public void configure(HttpSecurity http) {
    try {
      http.oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/user-info", true));
    } catch (Exception e) {
      throw new RuntimeException("Oatuh2 Error", e);
    }
  }
}
