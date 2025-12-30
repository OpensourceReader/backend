package com.opensourcereader.core.board.exception;

public class IssueNotFoundException extends BoardException {

  public IssueNotFoundException() {
    super(BoardErrorCode.ISSUE_NOT_FOUND);
  }
}
