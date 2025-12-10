package com.opensourcereader.core.issue.exception.issue;

import com.opensourcereader.core.issue.exception.IssueErrorCode;
import com.opensourcereader.core.issue.exception.IssueException;

public class IssueNotFoundException extends IssueException {

  public IssueNotFoundException() {
    super(IssueErrorCode.ISSUE_NOT_FOUND);
  }
}
