package com.opensourcereader.api.security.login;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.opensourcereader.api.security.jwt.JwtLogoutHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DefaultLogoutSuccessHandler implements LogoutSuccessHandler {

  private final JwtLogoutHandler jwtLogoutHandler;

  @Override
  public void onLogoutSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    jwtLogoutHandler.logout(request, response, authentication);

    // TODO 깃허브 Oauth2 구현해야함

    response.setStatus(HttpStatus.OK.value());
  }
}
