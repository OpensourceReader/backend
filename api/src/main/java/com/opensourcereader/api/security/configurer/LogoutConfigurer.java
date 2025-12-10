package com.opensourcereader.api.security.configurer;

import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

import com.opensourcereader.api.security.SecurityConfigurer;
import com.opensourcereader.api.security.jwt.JwtLogoutHandler;
import com.opensourcereader.api.security.login.DefaultLogoutSuccessHandler;

import lombok.RequiredArgsConstructor;

@Component
@Order
@RequiredArgsConstructor
public class LogoutConfigurer implements SecurityConfigurer {

  private final JwtLogoutHandler jwtLogoutHandler;
  private final DefaultLogoutSuccessHandler logoutSuccessHandler; // 아까 만든 클래스 주입

  @Override
  public void configure(HttpSecurity http) {
    try {
      http.logout(
          logout -> {
            logout
                .addLogoutHandler(jwtLogoutHandler)
                .logoutSuccessHandler(logoutSuccessHandler); // 여기서 사용
          });
    } catch (Exception e) {
      throw new RuntimeException("Logout Configuration Error", e);
    }
  }
}
