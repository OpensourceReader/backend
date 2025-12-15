package com.opensourcereader.core.board.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum BoardErrorCode {
  ISSUE_NOT_FOUND(HttpStatus.NOT_FOUND, "이슈를 찾을 수 없습니다.");

  private final HttpStatus httpStatus;
  private final String message;

  BoardErrorCode(HttpStatus httpStatus, String message) {
    this.httpStatus = httpStatus;
    this.message = message;
  }
}
