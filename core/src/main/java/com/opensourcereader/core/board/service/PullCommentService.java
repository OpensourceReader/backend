package com.opensourcereader.core.board.service;

import java.util.List;

import com.opensourcereader.core.board.entity.PullComment;

public interface PullCommentService {

  Long coundAllByReviewId(Long reviewId);

  List<PullComment> findAllByReviewId(Long reviewId);
}
