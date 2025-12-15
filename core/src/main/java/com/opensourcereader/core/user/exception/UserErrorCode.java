package com.opensourcereader.core.user.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum UserErrorCode {
  USER_AlREADY_EXIST(HttpStatus.INTERNAL_SERVER_ERROR, "유저가 이미 있습니다."),
  USER__NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다.");

  private final HttpStatus httpStatus;
  private final String message;

  UserErrorCode(HttpStatus httpStatus, String message) {
    this.httpStatus = httpStatus;
    this.message = message;
  }
}
