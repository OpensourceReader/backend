package com.opensourcereader.core.issue.exception;

public class IssueNotFoundException extends IssueException {

  public IssueNotFoundException() {
    super(IssueErrorCode.ISSUE_NOT_FOUND);
  }
}
