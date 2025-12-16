package com.opensourcereader.core.board.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.board.entity.IssueComment;
import com.opensourcereader.core.board.repository.IssueCommentRepository;
import com.opensourcereader.core.board.service.IssueCommentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueCommentServiceImpl implements IssueCommentService {

  private final IssueCommentRepository issueCommentRepository;

  @Override
  public Long countAllByIssueId(Long issueId) {
    return issueCommentRepository.countAllByIssueId(issueId);
  }

  @Override
  public List<IssueComment> findAllByIssueId(Long issueId) {
    return issueCommentRepository.findAllByIssueId(issueId);
  }
}
