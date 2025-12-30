package com.opensourcereader.api.facade.auth;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.opensourcereader.api.controller.auth.request.SignUpRequest;
import com.opensourcereader.core.security.dto.UserConnection;
import com.opensourcereader.core.user.dto.UserSignUpCommand;
import com.opensourcereader.core.user.entity.User;
import com.opensourcereader.core.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthFacadeService {

  private final UserService userService;

  public UserConnection signup(SignUpRequest request) {
    UserSignUpCommand command =
        new UserSignUpCommand(request.email(), request.password(), request.loginName(), null, null);
    User user = userService.signup(command);
    return new UserConnection(user, Instant.now());
  }
}
