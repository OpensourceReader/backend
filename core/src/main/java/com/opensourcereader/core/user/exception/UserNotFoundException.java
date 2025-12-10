package com.opensourcereader.core.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserNotFoundException extends ResponseStatusException {

  public UserNotFoundException(String nickname) {
    super(
        HttpStatus.NOT_FOUND,
        "User" + nickname + " was not found",
        null,
        null,
        new Object[] {nickname});
  }
}
