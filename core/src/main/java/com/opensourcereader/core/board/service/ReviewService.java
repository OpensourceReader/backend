package com.opensourcereader.core.board.service;

import com.opensourcereader.core.board.entity.Review;
import java.util.List;

public interface ReviewService {

  List<Review> findAllByPullId(Long pullId);

}
