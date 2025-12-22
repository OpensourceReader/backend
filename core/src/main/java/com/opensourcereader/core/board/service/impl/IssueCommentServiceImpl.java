package com.opensourcereader.core.board.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.board.dto.IssueCommentCommand;
import com.opensourcereader.core.board.entity.IssueComment;
import com.opensourcereader.core.board.exception.CommentNotFoundException;
import com.opensourcereader.core.board.repository.IssueCommentRepository;
import com.opensourcereader.core.board.service.IssueCommentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueCommentServiceImpl implements IssueCommentService {

  private final IssueCommentRepository issueCommentRepository;

  @Override
  @Transactional
  public IssueComment upsert(IssueCommentCommand command) {
    IssueComment entity = IssueComment.from(command);
    issueCommentRepository.upsert(entity);
    return entity;
  }

  @Override
  @Transactional(readOnly = true)
  public List<IssueComment> findAllByIssueId(Long issueId) {
    return issueCommentRepository.findAllByIssueId(issueId);
  }

  @Override
  @Transactional
  public void deleteSoftById(Long id) {
    IssueComment issueComment =
        issueCommentRepository.findById(id).orElseThrow(CommentNotFoundException::new);
    issueComment.updateDisabled(true);
    issueCommentRepository.save(issueComment);
  }
}
