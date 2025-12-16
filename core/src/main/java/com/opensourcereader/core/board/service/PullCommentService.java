package com.opensourcereader.core.board.service;

import com.opensourcereader.core.board.entity.PullComment;
import java.util.List;

public interface PullCommentService {

  List<PullComment> findAllByReviewId(Long reviewId);

}
