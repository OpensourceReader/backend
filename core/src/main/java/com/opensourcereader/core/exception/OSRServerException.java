package com.opensourcereader.core.exception;

import org.springframework.http.HttpStatus;

public class OSRServerException extends RuntimeException {

  public OSRServerException(String message) {
    super(message);
  }

  public OSRServerException(HttpStatus message) {
    super(message.getReasonPhrase());
  }
}
