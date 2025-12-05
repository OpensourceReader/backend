package com.opensourcereader.api.security.login;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensourcereader.api.controller.auth.request.LoginRequest;
import com.opensourcereader.core.exception.OSRServerException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private final ObjectMapper objectMapper;

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    try {
      LoginRequest loginRequest =
          objectMapper.readValue(request.getInputStream(), LoginRequest.class);

      UsernamePasswordAuthenticationToken token =
          new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());

      setDetails(request, token);
      return this.getAuthenticationManager().authenticate(token);
    } catch (IOException e) {
      throw new OSRServerException(HttpStatus.BAD_REQUEST);
    }
  }
}
