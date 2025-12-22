package com.opensourcereader.core.board.service;

import java.util.List;

import com.opensourcereader.core.board.dto.IssueCommentCommand;
import com.opensourcereader.core.board.entity.IssueComment;

public interface IssueCommentService {

  IssueComment upsert(IssueCommentCommand command);

  List<IssueComment> findAllByIssueId(Long issueId);

  void deleteSoftById(Long id);
}
