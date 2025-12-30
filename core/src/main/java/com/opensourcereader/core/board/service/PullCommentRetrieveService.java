package com.opensourcereader.core.board.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.board.entity.PullComment;
import com.opensourcereader.core.board.repository.PullCommentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PullCommentRetrieveService {

  private final PullCommentRepository pullCommentRepository;

  @Transactional(readOnly = true)
  public List<PullComment> findAllByReviewId(Long reviewId) {
    return pullCommentRepository.findAllByReviewId(reviewId);
  }
}
