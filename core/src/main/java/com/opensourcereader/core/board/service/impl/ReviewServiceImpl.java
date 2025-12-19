package com.opensourcereader.core.board.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.board.dto.ReviewCommand;
import com.opensourcereader.core.board.entity.Review;
import com.opensourcereader.core.board.exception.CommentNotFoundException;
import com.opensourcereader.core.board.repository.ReviewRepository;
import com.opensourcereader.core.board.service.ReviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;

  @Override
  public Review create(ReviewCommand command) {
    Review review =
        reviewRepository
            .findById(command.getId())
            .orElseGet(
                () -> {
                  Review entity = Review.from(command);
                  return reviewRepository.save(entity);
                });

    // TODO 만약 updatedAt가 command의 updatedAt랑 다르면 update 작업을 진행하는 로직 만들어야 함
    if (review.getUpdatedAt() != command.getUpdatedAt()) {
      log.info("업데이트 로직 필요");
    }
    return review;
  }

  @Override
  public List<Review> findAllByPullId(Long pullId) {
    return reviewRepository.findAllByPullId(pullId);
  }

  @Override
  public void deleteSoftById(Long id) {
    Review review = reviewRepository.findById(id).orElseThrow(CommentNotFoundException::new);
    review.updateDisabled(true);
    reviewRepository.save(review);
  }
}
