package com.opensourcereader.core.user.exception;

public class UserAlreadyExistException extends UserException {

  public UserAlreadyExistException() {
    super(UserErrorCode.USER_AlREADY_EXIST);
  }
}
