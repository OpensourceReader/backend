package com.opensourcereader.api.security.oauth2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.opensourcereader.core.security.entity.RefreshToken;
import com.opensourcereader.core.security.service.JwtService;
import com.opensourcereader.core.security.service.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Oauth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Authentication authentication)
      throws IOException, ServletException {
    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

    Map<String, Object> attributes = oAuth2User.getAttributes();
    String nickname = (String) attributes.get("username");
    String email = (String) attributes.get("email");

    String accessToken = jwtService.encoder(nickname, email);
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(nickname);

    String targetUrl =
        UriComponentsBuilder.fromUriString("http://localhost:8080/oauth/callback")
            .queryParam("accessToken", accessToken)
            .queryParam("refreshToken", refreshToken.getToken())
            .build()
            .encode(StandardCharsets.UTF_8)
            .toUriString();

    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }
}
