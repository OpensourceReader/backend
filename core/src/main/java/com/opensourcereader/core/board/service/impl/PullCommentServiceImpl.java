package com.opensourcereader.core.board.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  @Transactional
  public PullComment upsert(PullCommentCommand command) {
    PullComment entity = PullComment.from(command);
    pullCommentRepository.upsert(entity);
    return entity;
  }

  @Override
  @Transactional(readOnly = true)
  public List<PullComment> findAllByReviewId(Long reviewId) {
    return pullCommentRepository.findAllByReviewId(reviewId);
  }

  @Override
  @Transactional
  public void deleteSoftById(Long id) {
    PullComment pullComment =
        pullCommentRepository.findById(id).orElseThrow(CommentNotFoundException::new);
    pullComment.updateDisabled(true);
    pullCommentRepository.save(pullComment);
  }
}
