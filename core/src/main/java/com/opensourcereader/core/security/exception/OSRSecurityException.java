package com.opensourcereader.core.security.exception;

import com.opensourcereader.core.exception.OpenSourceReaderException;

import lombok.Getter;

@Getter
public abstract class OSRSecurityException extends OpenSourceReaderException {

  private final SecurityErrorCode securityErrorCode;

  public OSRSecurityException(SecurityErrorCode securityErrorCode) {
    this.securityErrorCode = securityErrorCode;
  }
}
