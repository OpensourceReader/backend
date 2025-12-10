package com.opensourcereader.api.security.jwt;

import java.util.Arrays;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.opensourcereader.core.security.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final RefreshTokenService refreshTokenService;

  @Override
  public void logout(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return;
    }

    Arrays.stream(cookies)
        .filter(cookie -> cookie.getName().equals(RefreshTokenService.REFRESH_TOKEN_NAME))
        .findFirst()
        .map(Cookie::getValue)
        .ifPresent(
            refreshToken -> {
              refreshTokenService.deleteToken(refreshToken);
              invalidateRefreshTokenCookie(response);
            });
  }

  private void invalidateRefreshTokenCookie(HttpServletResponse response) {
    Cookie cookie = new Cookie(RefreshTokenService.REFRESH_TOKEN_NAME, "");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(0);
    cookie.setPath("/");

    response.addCookie(cookie);
  }
}
