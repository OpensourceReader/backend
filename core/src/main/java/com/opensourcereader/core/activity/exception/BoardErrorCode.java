package com.opensourcereader.core.activity.exception;

import org.springframework.http.HttpStatus;

import com.opensourcereader.core.shared.exception.BaseErrorCode;

import lombok.Getter;

@Getter
public enum BoardErrorCode implements BaseErrorCode {
  BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "이슈와 PR 검색에 실패했습니다."),
  ISSUE_NOT_FOUND(HttpStatus.NOT_FOUND, "이슈를 찾을 수 없습니다."),
  PULL_NOT_FOUND(HttpStatus.NOT_FOUND, "PULL Request를 찾을 수 없습니다."),
  COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Comment를 찾을 . 없습니다.");

  private final HttpStatus httpStatus;
  private final String message;
  private final String name;

  BoardErrorCode(HttpStatus httpStatus, String message) {
    this.httpStatus = httpStatus;
    this.message = message;
    this.name = this.name();
  }
}
