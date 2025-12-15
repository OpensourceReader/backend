package com.opensourcereader.core.issue.exception;

import com.opensourcereader.core.exception.OpenSourceReaderException;

import lombok.Getter;

@Getter
public abstract class IssueException extends OpenSourceReaderException {

  private final IssueErrorCode issueErrorCode;

  public IssueException(IssueErrorCode issueErrorCode) {
    this.issueErrorCode = issueErrorCode;
  }
}
