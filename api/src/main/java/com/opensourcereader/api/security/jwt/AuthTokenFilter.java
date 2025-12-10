package com.opensourcereader.api.security.jwt;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.opensourcereader.api.security.DefaultUserDetailService;
import com.opensourcereader.core.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final DefaultUserDetailService userDetailService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String token = parseJwt(request);
    if (token != null) {
      try {
        // validate 및 decode
        String nickname = jwtService.decode(token);
        UserDetails user = userDetailService.loadUserByUsername(nickname);
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      } catch (Exception e) {
        // 실패, 더이상 진행하면 안됨
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
      }
    }

    filterChain.doFilter(request, response);
  }

  private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader(HttpHeaders.AUTHORIZATION);
    String prefix = "Bearer ";
    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(prefix)) {
      return headerAuth.substring(prefix.length());
    }
    return null;
  }
}
