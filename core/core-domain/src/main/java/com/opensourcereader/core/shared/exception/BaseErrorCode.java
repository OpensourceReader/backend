package com.opensourcereader.core.shared.exception;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {

  HttpStatus getHttpStatus();

  String getMessage();

  String getName();
}
