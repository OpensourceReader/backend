package com.opensourcereader.core.security.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum SecurityErrorCode {
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "허용되지 않은 접근입니다."),
  TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다.");

  private final HttpStatus httpStatus;
  private final String message;

  SecurityErrorCode(HttpStatus httpStatus, String message) {
    this.httpStatus = httpStatus;
    this.message = message;
  }
}
