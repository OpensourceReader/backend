package com.opensourcereader.core.user.exception;

import com.opensourcereader.core.exception.OpenSourceReaderException;

import lombok.Getter;

@Getter
public abstract class UserException extends OpenSourceReaderException {

  private final UserErrorCode userErrorCode;

  public UserException(UserErrorCode userErrorCode) {
    this.userErrorCode = userErrorCode;
  }
}
