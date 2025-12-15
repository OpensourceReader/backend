package com.opensourcereader.core.issue.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.issue.entity.IssueComment;
import com.opensourcereader.core.issue.repository.IssueCommentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueCommentServiceImpl implements IssueCommentService {

  private final IssueCommentRepository issueCommentRepository;

  @Override
  public List<IssueComment> findAllByIssueId(Long issueId) {
    return issueCommentRepository.findAllByIssueId(issueId);
  }
}
