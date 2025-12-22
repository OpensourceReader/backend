package com.opensourcereader.core.board.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.board.dto.IssueCommentCommand;
import com.opensourcereader.core.board.entity.IssueComment;
import com.opensourcereader.core.board.exception.CommentNotFoundException;
import com.opensourcereader.core.board.repository.IssueCommentRepository;
import com.opensourcereader.core.board.service.IssueCommentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueCommentServiceImpl implements IssueCommentService {

  private final IssueCommentRepository issueCommentRepository;

  @Override
  @Transactional
  public IssueComment create(IssueCommentCommand command) {
    IssueComment issueComment =
        issueCommentRepository
            .findById(command.getId())
            .orElseGet(
                () -> {
                  IssueComment entity = IssueComment.from(command);
                  return issueCommentRepository.save(entity);
                });

    // TODO 만약 updatedAt가 command의 updatedAt랑 다르면 update 작업을 진행하는 로직 만들어야 함
    if (issueComment.getUpdatedAt() != command.getUpdatedAt()) {
      log.info("업데이트 로직 필요");
    }

    return issueComment;
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
