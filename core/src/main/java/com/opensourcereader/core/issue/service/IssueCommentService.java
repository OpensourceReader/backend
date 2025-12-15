package com.opensourcereader.core.issue.service;

import java.util.List;

import com.opensourcereader.core.issue.entity.IssueComment;

public interface IssueCommentService {

  List<IssueComment> findAllByIssueId(Long issueId);
}
