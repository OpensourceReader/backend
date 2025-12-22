package com.opensourcereader.core.board.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  @Transactional
  public Review upsert(ReviewCommand command) {
    Review entity = Review.from(command);
    reviewRepository.upsert(entity);
    return entity;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Review> findAllByPullId(Long pullId) {
    return reviewRepository.findAllByPullId(pullId);
  }

  @Override
  @Transactional
  public void deleteSoftById(Long id) {
    Review review = reviewRepository.findById(id).orElseThrow(CommentNotFoundException::new);
    review.updateDisabled(true);
    reviewRepository.save(review);
  }
}
