package com.opensourcereader.core.board.service.impl;

import com.opensourcereader.core.board.entity.Review;
import com.opensourcereader.core.board.repository.ReviewRepository;
import com.opensourcereader.core.board.service.ReviewService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;

  @Override
  public List<Review> findAllByPullId(Long pullId) {
    return reviewRepository.findAllByPullId(pullId);
  }
}
