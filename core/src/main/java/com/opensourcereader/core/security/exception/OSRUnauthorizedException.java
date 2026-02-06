package com.opensourcereader.core.security.exception;

import lombok.Getter;

@Getter
public class OSRUnauthorizedException extends OSRSecurityException {

  public OSRUnauthorizedException() {
    super(SecurityErrorCode.UNAUTHORIZED);
  }
}
