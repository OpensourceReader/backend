package com.opensourcereader.api.security.jwt;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensourcereader.api.security.login.OSRUser;
import com.opensourcereader.core.security.entity.RefreshToken;
import com.opensourcereader.core.security.service.JwtService;
import com.opensourcereader.core.security.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final ObjectMapper objectMapper;
  private final RefreshTokenService refreshTokenService;
  private final JwtService jwtService;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    OSRUser principal = (OSRUser) authentication.getPrincipal();
    String nickname = principal.getUsername();

    // TODO 중복 로그인 파트: 기기 1개당 1개 로그인 가능 -> 추후 동시에 몇 개의 로그인까지 가능할 지 협의 필요
    refreshTokenService.invalidate(nickname);
    String accessToken = jwtService.encoder(principal.getName(), nickname);
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(nickname);

    // refresh 쿠키 설정
    Cookie cookie = new Cookie(RefreshTokenService.REFRESH_TOKEN_NAME, refreshToken.getToken());
    cookie.setHttpOnly(true);
    cookie.setMaxAge(7 * 24 * 60 * 60);
    cookie.setPath("/");
    response.addCookie(cookie);

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    response.getWriter().write(objectMapper.writeValueAsString(accessToken));
  }
}
