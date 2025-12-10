package com.opensourcereader.core.issue.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum IssueErrorCode {
  ISSUE_NOT_FOUND(HttpStatus.NOT_FOUND, "이슈를 찾을 수 없습니다.");

  private final HttpStatus httpStatus;
  private final String message;

  IssueErrorCode(HttpStatus httpStatus, String message) {
    this.httpStatus = httpStatus;
    this.message = message;
  }
}
