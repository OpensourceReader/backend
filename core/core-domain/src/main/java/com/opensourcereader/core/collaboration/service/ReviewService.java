package com.opensourcereader.core.collaboration.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.collaboration.dto.ReviewCommand;
import com.opensourcereader.core.collaboration.entity.Review;
import com.opensourcereader.core.collaboration.exception.BoardNotFoundException;
import com.opensourcereader.core.collaboration.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewRepository reviewRepository;

  @Transactional
  public boolean saveReviews(List<ReviewCommand> commands) {
    List<Review> entities = new ArrayList<>();
    for (ReviewCommand command : commands) {
      Review review = Review.from(command);
      entities.add(review);
    }
    reviewRepository.saveAll(entities);
    return true;
  }

  @Transactional(readOnly = true)
  public List<Review> findAllByPullId(Long pullId) {
    return reviewRepository.findAllByPullId(pullId);
  }

  @Transactional(readOnly = true)
  public Review findByProviderId(Long reviewId) {
    return reviewRepository.findByProviderId(reviewId).orElseThrow(BoardNotFoundException::new);
  }
}
