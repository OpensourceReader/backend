package com.opensourcereader.core.security.exception;

import com.opensourcereader.core.shared.exception.OpenSourceReaderException;

import lombok.Getter;

@Getter
public abstract class OSRSecurityException extends OpenSourceReaderException {

  public OSRSecurityException(SecurityErrorCode securityErrorCode) {
    super(securityErrorCode);
  }
}
