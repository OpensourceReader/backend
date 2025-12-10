package com.opensourcereader.api.controller.auth.request;

import com.opensourcereader.core.security.dto.SignUpCommand;

public record SignUpRequest(String email, String password, String username) {
  public SignUpCommand toCommand() {
    return new SignUpCommand(email, password, username);
  }
}
