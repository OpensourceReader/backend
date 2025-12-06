package com.opensourcereader.core.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserNotFoundException extends ResponseStatusException {

  public UserNotFoundException(String email) {
    super(
        HttpStatus.NOT_FOUND, "User" + email + " was not found", null, null, new Object[] {email});
  }
}
