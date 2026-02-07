package com.opensourcereader.core.activity.exception;

public class IssueNotFoundException extends BoardException {

  public IssueNotFoundException() {
    super(BoardErrorCode.ISSUE_NOT_FOUND);
  }
}
