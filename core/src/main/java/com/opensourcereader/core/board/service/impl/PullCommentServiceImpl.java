package com.opensourcereader.core.board.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.board.dto.PullCommentCommand;
import com.opensourcereader.core.board.entity.PullComment;
import com.opensourcereader.core.board.exception.CommentNotFoundException;
import com.opensourcereader.core.board.repository.PullCommentRepository;
import com.opensourcereader.core.board.service.PullCommentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PullCommentServiceImpl implements PullCommentService {

  private final PullCommentRepository pullCommentRepository;

  @Override
  public PullComment create(PullCommentCommand command) {
    PullComment pullComment =
        pullCommentRepository
            .findById(command.getId())
            .orElseGet(
                () -> {
                  PullComment entity = PullComment.from(command);
                  return pullCommentRepository.save(entity);
                });
    // TODO 만약 updatedAt가 command의 updatedAt랑 다르면 update 작업을 진행하는 로직 만들어야 함
    if (pullComment.getUpdatedAt() != command.getUpdatedAt()) {
      log.info("업데이트 로직 필요");
    }

    return pullComment;
  }

  @Override
  public List<PullComment> findAllByReviewId(Long reviewId) {
    return pullCommentRepository.findAllByReviewId(reviewId);
  }

  @Override
  public void deleteSoftById(Long id) {
    PullComment pullComment =
        pullCommentRepository.findById(id).orElseThrow(CommentNotFoundException::new);
    pullComment.updateDisabled(true);
    pullCommentRepository.save(pullComment);
  }
}
