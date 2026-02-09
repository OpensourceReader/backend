package com.opensourcereader.core.user.exception;

import com.opensourcereader.core.shared.exception.OpenSourceReaderException;

import lombok.Getter;

@Getter
public abstract class UserException extends OpenSourceReaderException {

  public UserException(UserErrorCode userErrorCode) {
    super(userErrorCode);
  }
}
