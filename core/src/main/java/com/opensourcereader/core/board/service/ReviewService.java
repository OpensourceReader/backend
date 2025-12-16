package com.opensourcereader.core.board.service;

import java.util.List;

import com.opensourcereader.core.board.entity.Review;

public interface ReviewService {

  Long countAllByPullId(Long pullId);

  List<Review> findAllByPullId(Long pullId);
}
