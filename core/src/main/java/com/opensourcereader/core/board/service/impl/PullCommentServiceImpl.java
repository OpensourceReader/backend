package com.opensourcereader.core.board.service.impl;

import com.opensourcereader.core.board.entity.PullComment;
import com.opensourcereader.core.board.repository.PullCommentRepository;
import com.opensourcereader.core.board.service.PullCommentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PullCommentServiceImpl implements PullCommentService {

  private final PullCommentRepository pullCommentRepository;

  @Override
  public List<PullComment> findAllByReviewId(Long reviewId) {
    return pullCommentRepository.findAllByReviewId(reviewId);
  }
}
