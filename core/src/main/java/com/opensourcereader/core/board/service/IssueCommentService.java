package com.opensourcereader.core.board.service;

import java.util.List;

import com.opensourcereader.core.board.entity.IssueComment;

public interface IssueCommentService {

  List<IssueComment> findAllByIssueId(Long issueId);
}
