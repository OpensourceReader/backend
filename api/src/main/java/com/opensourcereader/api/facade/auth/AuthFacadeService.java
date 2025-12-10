package com.opensourcereader.api.facade.auth;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.security.dto.SignUpCommand;
import com.opensourcereader.core.security.dto.UserConnection;
import com.opensourcereader.core.security.service.AuthService;
import com.opensourcereader.core.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthFacadeService {

  private final AuthService authService;

  public UserConnection signup(SignUpCommand command) {
    User user = authService.signup(command);
    return new UserConnection(user, Instant.now());
  }
}
