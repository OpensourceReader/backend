package com.opensourcereader.api.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opensourcereader.api.controller.auth.request.SignUpRequest;
import com.opensourcereader.api.facade.auth.AuthFacadeService;
import com.opensourcereader.core.security.dto.SignUpCommand;
import com.opensourcereader.core.security.dto.UserConnection;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthFacadeService authFacadeService;

  @PostMapping("/signup")
  public ResponseEntity<UserConnection> signUpOrGet(@RequestBody SignUpRequest request) {
    SignUpCommand command = request.toCommand();
    UserConnection connection = authFacadeService.signup(command);

    return ResponseEntity.ok(connection);
  }
}
