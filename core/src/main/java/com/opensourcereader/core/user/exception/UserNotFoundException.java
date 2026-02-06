package com.opensourcereader.core.user.exception;

public class UserNotFoundException extends UserException {

  public UserNotFoundException() {
    super(UserErrorCode.USER__NOT_FOUND);
  }
}
