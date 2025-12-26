package com.opensourcereader.core.board.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.board.entity.IssueComment;
import com.opensourcereader.core.board.repository.IssueCommentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueCommentRetrieveService {

  private final IssueCommentRepository issueCommentRepository;

  @Transactional(readOnly = true)
  public List<IssueComment> findAllByIssueId(Long issueId) {
    return issueCommentRepository.findAllByIssueId(issueId);
  }
}
