package com.opensourcereader.core.board.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.board.entity.PullComment;
import com.opensourcereader.core.board.repository.PullCommentRepository;
import com.opensourcereader.core.board.service.PullCommentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PullCommentServiceImpl implements PullCommentService {

  private final PullCommentRepository pullCommentRepository;

  @Override
  public Long coundAllByReviewId(Long reviewId) {
    return pullCommentRepository.countAllByReviewId(reviewId);
  }

  @Override
  public List<PullComment> findAllByReviewId(Long reviewId) {
    return pullCommentRepository.findAllByReviewId(reviewId);
  }
}
