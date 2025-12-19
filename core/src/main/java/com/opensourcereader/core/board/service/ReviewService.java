package com.opensourcereader.core.board.service;

import java.util.List;

import com.opensourcereader.core.board.dto.ReviewCommand;
import com.opensourcereader.core.board.entity.Review;

public interface ReviewService {

  Review create(ReviewCommand command);

  List<Review> findAllByPullId(Long pullId);

  void deleteSoftById(Long id);
}
