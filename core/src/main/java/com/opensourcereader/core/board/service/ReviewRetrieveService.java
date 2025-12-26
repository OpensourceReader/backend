package com.opensourcereader.core.board.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.board.entity.Review;
import com.opensourcereader.core.board.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewRetrieveService {

  private final ReviewRepository reviewRepository;

  @Transactional(readOnly = true)
  public List<Review> findAllByPullId(Long pullId) {
    return reviewRepository.findAllByPullId(pullId);
  }
}
