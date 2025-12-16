package com.opensourcereader.core.board.service;

import java.util.List;

import com.opensourcereader.core.board.entity.IssueComment;

public interface IssueCommentService {

  Long countAllByIssueId(Long issueId);

  List<IssueComment> findAllByIssueId(Long issueId);
}
