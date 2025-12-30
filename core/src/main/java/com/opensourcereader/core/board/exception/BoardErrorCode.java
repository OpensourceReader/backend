package com.opensourcereader.core.board.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum BoardErrorCode {
  BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "이슈와 PR 검색에 실패했습니다."),
  ISSUE_NOT_FOUND(HttpStatus.NOT_FOUND, "이슈를 찾을 수 없습니다."),
  PULL_NOT_FOUND(HttpStatus.NOT_FOUND, "PULL Request를 찾을 수 없습니다."),
  COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Comment를 찾을 . 없습니다.");

  private final HttpStatus httpStatus;
  private final String message;

  BoardErrorCode(HttpStatus httpStatus, String message) {
    this.httpStatus = httpStatus;
    this.message = message;
  }
}
