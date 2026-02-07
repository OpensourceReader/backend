package com.opensourcereader.core.collaboration.exception;

public class IssueNotFoundException extends BoardException {

  public IssueNotFoundException() {
    super(BoardErrorCode.ISSUE_NOT_FOUND);
  }
}
